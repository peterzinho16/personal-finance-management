package com.bindord.financemanagement.repository;

import com.bindord.financemanagement.model.dashboard.CategoryMonthlyTotalsProjection;
import com.bindord.financemanagement.model.dashboard.MonthlyExpenseSummaryDTO;
import com.bindord.financemanagement.model.finance.Expenditure;
import com.bindord.financemanagement.model.resume.ResumeSummaryProjection;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ExpenditureRepository extends JpaRepository<Expenditure, Integer> {

  @Query(value = "SELECT EX FROM Expenditure EX JOIN FETCH EX.subCategory SC WHERE EX.id = ?1")
  Optional<Expenditure> findById(Integer id);

  @Query(value = "SELECT E.referenceId FROM Expenditure E WHERE E.referenceId IN (?1)")
  Set<String> findByReferenceIdIn(Set<String> refIds);

  @Query(value = "SELECT EX from Expenditure EX " +
      "JOIN FETCH EX.subCategory SC " +
      "JOIN FETCH SC.category " +
      "WHERE (:subCategoryId IS NULL OR SC.id = :subCategoryId) " +
      "ORDER BY EX.transactionDate DESC")
  Page<Expenditure> findAllWithSubCategory(@Param("subCategoryId") Integer subCategoryId,
                                           Pageable pageable);

  @Query(value = "SELECT EX from Expenditure EX JOIN FETCH EX.subCategory SC JOIN FETCH " +
      "SC.category WHERE EX.id = ?1")
  Expenditure findByIdWithSubCategory(Integer id);

  @Transactional
  @Modifying
  @Query(value = "UPDATE expenditures SET sub_category_id = :subCategoryId WHERE " +
      "id = :id", nativeQuery = true)
  void updateSubCategoryById(@Param("subCategoryId") Integer subCategoryId,
                             @Param("id") Integer id);

  @Query(value = "SELECT EX from Expenditure EX JOIN FETCH EX.subCategory SC JOIN FETCH " +
      "SC.category WHERE EX.expenditureInstallmentId = ?1 ORDER BY EX.id")
  List<Expenditure> findAllByExpenditureInstallmentIdOrderById(Integer expenditureInstallmentId);


  @Query(value = """
      WITH category_monthly_totals AS (
          SELECT
              to_char(e.transaction_date, 'YYYY-MM') AS periodo,
              c.name AS categoria,
              round(
                  sum(CASE
                      WHEN e.shared IS FALSE AND e.lent IS FALSE AND e.was_borrowed IS FALSE
                      THEN CASE
                          WHEN e.currency = 'PEN' THEN e.amount
                          ELSE conversion_to_pen
                      END
                      ELSE 0
                  END)::NUMERIC, 2) AS gastos_individuales,
              round(
                  sum(CASE
                      WHEN e.shared IS TRUE
                      THEN CASE
                          WHEN e.currency = 'PEN' THEN e.shared_amount
                          ELSE conversion_to_pen / 2
                      END
                      ELSE 0
                  END)::NUMERIC, 2) AS gastos_compartidos
          FROM expenditures e
          JOIN sub_categories sc ON sc.id = e.sub_category_id
          JOIN categories c ON c.id = sc.category_id
          GROUP BY to_char(e.transaction_date, 'YYYY-MM'), c.name
      )
      SELECT * FROM category_monthly_totals ORDER BY periodo DESC, categoria
      """, nativeQuery = true)
  List<CategoryMonthlyTotalsProjection> getCategoryMonthlyTotals();

  @Query(value = "SELECT EX from Expenditure EX " +
      "JOIN FETCH EX.subCategory SC " +
      "JOIN FETCH SC.category C " +
      "WHERE EX.transactionDate BETWEEN :initMonth AND :endMonthPlusOne " +
      "AND C.id = :categoryId " +
      "AND EX.shared = :shared")
  List<Expenditure> findAllByYearMonthAndCategoryAndShared(LocalDateTime initMonth,
                                                           LocalDateTime endMonthPlusOne,
                                                           Integer categoryId, Boolean shared);

  @Query(value = """
      WITH subtot AS (
              SELECT to_char(transaction_date, 'YYYY-MM') AS periodo,
                     round(sum(CASE
                                   WHEN shared IS FALSE AND lent IS FALSE AND was_borrowed IS FALSE AND exp_imported IS FALSE
                                   THEN CASE WHEN currency = 'PEN' THEN amount
                                             ELSE conversion_to_pen END
                                   ELSE 0 END)::NUMERIC, 2) AS gastos_individuales,
                     round(sum(CASE WHEN shared IS TRUE THEN
                                       CASE WHEN currency = 'PEN' THEN shared_amount
                                            ELSE conversion_to_pen / 2 END
                                   ELSE 0 END)::NUMERIC, 2) AS gastos_compartidos,
                     round(sum(CASE WHEN was_borrowed IS TRUE THEN
                                       CASE WHEN currency = 'PEN' THEN amount
                                            ELSE conversion_to_pen END
                                   ELSE 0 END)::NUMERIC, 2) AS mis_gastos_pagados_por_tercero,
                     round(sum(CASE
                                   WHEN exp_imported IS TRUE
                                       THEN CASE
                                                WHEN currency = 'PEN' THEN shared_amount
                                                ELSE conversion_to_pen / 2
                                       END
                                   ELSE 0
                         END)::NUMERIC, 2)                            AS Mis_Gastos_Importados,
                     round(sum(CASE WHEN lent IS TRUE THEN
                                       CASE WHEN currency = 'PEN' THEN loan_amount
                                            ELSE conversion_to_pen END
                                   ELSE 0 END)::NUMERIC, 2) AS total_tus_prestamos,
                     (SELECT sum(amount) FROM recurrent_expenditures) AS gastos_recurrentes_total
              FROM expenditures
              GROUP BY to_char(transaction_date, 'YYYY-MM')
          )
      SELECT
          (SELECT round(SUM(CASE WHEN currency = 'PEN' THEN amount
                                 ELSE conversion_to_pen END)::NUMERIC, 2)
           FROM incomes
           WHERE was_received AND to_char(received_date, 'YYYY-MM') = subtot.periodo) AS otros_ingresos,
          (subtot.gastos_individuales + subtot.gastos_compartidos + subtot.mis_gastos_pagados_por_tercero + subtot.Mis_Gastos_Importados) AS final_total_gastos,
          subtot.periodo,
          subtot.gastos_individuales,
          subtot.gastos_compartidos,
          subtot.mis_gastos_pagados_por_tercero,
          subtot.mis_gastos_importados,
          subtot.total_tus_prestamos,
          subtot.gastos_recurrentes_total
      FROM subtot
      ORDER BY subtot.periodo DESC
      """, nativeQuery = true)
  List<MonthlyExpenseSummaryDTO> getMonthlySummary();


  @Query(value = """
        WITH subtot AS (
            SELECT
                date_trunc('month', transaction_date)::date AS periodo,
                ROUND(SUM(
                    CASE
                        WHEN NOT shared AND NOT lent AND NOT was_borrowed AND NOT exp_imported THEN
                            CASE WHEN currency = 'PEN' THEN amount ELSE conversion_to_pen END
                        ELSE 0 END
                )::NUMERIC, 2) AS gastos_individuales,
                ROUND(SUM(
                    CASE
                        WHEN shared AND (exp_imported IS FALSE OR exp_imported IS NULL) THEN
                            CASE WHEN currency = 'PEN' THEN shared_amount ELSE conversion_to_pen / 2 END
                        ELSE 0 END
                )::NUMERIC, 2) AS gastos_compartidos,
                ROUND(SUM(
                    CASE
                        WHEN was_borrowed THEN
                            CASE WHEN currency = 'PEN' THEN amount ELSE conversion_to_pen END
                        ELSE 0 END
                )::NUMERIC, 2) AS mis_gastos_pagados_por_tercero,
                ROUND(SUM(
                    CASE
                        WHEN exp_imported THEN
                            CASE WHEN currency = 'PEN' THEN shared_amount ELSE conversion_to_pen / 2 END
                        ELSE 0 END
                )::NUMERIC, 2) AS mis_gastos_importados,
                ROUND(SUM(
                    CASE
                        WHEN lent THEN
                            CASE WHEN currency = 'PEN' THEN loan_amount ELSE conversion_to_pen END
                        ELSE 0 END
                )::NUMERIC, 2) AS total_tus_prestamos,
                (SELECT SUM(amount) FROM recurrent_expenditures) AS gastos_recurrentes_total
            FROM expenditures
            WHERE transaction_date >= :start_date
              AND transaction_date < :end_date
            GROUP BY 1
        )
        SELECT
            TO_CHAR(s.periodo, 'YYYY-MM') AS periodo,
            COALESCE((
                SELECT ROUND(SUM(
                    CASE WHEN currency = 'PEN' THEN amount ELSE conversion_to_pen END
                )::NUMERIC, 2)
                FROM incomes i
                WHERE i.was_received
                  AND i.received_date >= :start_date
                  AND i.received_date < :end_date
            ), 0.0) AS otros_ingresos,
            COALESCE(s.gastos_individuales + s.gastos_compartidos + s.mis_gastos_pagados_por_tercero, 0.0) AS final_total_gastos,
            COALESCE(s.gastos_compartidos, 0.0) AS gastos_compartidos,
            COALESCE(s.mis_gastos_importados, 0.0) AS mis_gastos_compartidos_importados,
            COALESCE(s.gastos_compartidos - s.mis_gastos_importados, 0.0) AS gastos_a_devolver
        FROM subtot s;
        """, nativeQuery = true)
  List<ResumeSummaryProjection> getMonthlySummary(
      @Param("start_date") LocalDate startDate,
      @Param("end_date") LocalDate endDate
  );
}

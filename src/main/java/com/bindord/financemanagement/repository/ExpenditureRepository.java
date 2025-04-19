package com.bindord.financemanagement.repository;

import com.bindord.financemanagement.model.dashboard.CategoryMonthlyTotalsProjection;
import com.bindord.financemanagement.model.finance.Expenditure;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
  void updateSubCategoryByPayeeId(@Param("subCategoryId") Integer subCategoryId,
                                  @Param("id") Integer id);

  @Query(value = "SELECT EX from Expenditure EX JOIN FETCH EX.subCategory SC JOIN FETCH " +
      "SC.category WHERE EX.expenditureInstallmentId = ?1 ORDER BY EX.id")
  List<Expenditure> findAllByExpenditureInstallmentIdOrderById(Integer expenditureInstallmentId);


  @Query(value = """
      WITH constants AS (
          SELECT 3.67 AS exchange_rate
      ),
      category_monthly_totals AS (
          SELECT
              to_char(e.transaction_date, 'YYYY-MM') AS periodo,
              c.name AS categoria,
              round(
                  sum(CASE
                      WHEN e.shared IS FALSE AND e.lent IS FALSE AND e.was_borrowed IS FALSE
                      THEN CASE
                          WHEN e.currency = 'PEN' THEN e.amount
                          ELSE e.amount * (SELECT exchange_rate FROM constants)
                      END
                      ELSE 0
                  END)::NUMERIC, 2) AS gastos_individuales,
              round(
                  sum(CASE
                      WHEN e.shared IS TRUE
                      THEN CASE
                          WHEN e.currency = 'PEN' THEN e.shared_amount
                          ELSE e.shared_amount * (SELECT exchange_rate FROM constants)
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
}

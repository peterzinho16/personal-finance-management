package com.bindord.financemanagement.repository;

import com.bindord.financemanagement.model.finance.PayeeCategorization;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface PayeeCategorizationRepository extends JpaRepository<PayeeCategorization, Integer> {

  @Transactional
  @Modifying
  @Query(value = "INSERT INTO payee_categorizations (payee, creation_date, sub_category_id, " +
      "total_events) " +
      "VALUES (:payee, :creationDate, :subCategoryId, 1) ON CONFLICT DO NOTHING", nativeQuery =
      true)
  void insertPayeeCategorizationAndDoNothingOnConflict(@Param("payee") String payee,
                                                       @Param("creationDate") LocalDateTime creationDate,
                                                       @Param("subCategoryId") Integer subCategoryId);

  @Query(nativeQuery = true, value = "SELECT sub_category_id FROM payee_categorizations " +
      "WHERE lower_payee = :lowerPayee")
  Integer findSubCategoryIdByLowerPayee(@Param("lowerPayee") String lowerPayee);

  @Query(nativeQuery = true, value = "SELECT payee_id FROM payee_categorizations " +
      "WHERE lower_payee = :lowerPayee")
  Integer findIdByLowerPayee(@Param("lowerPayee") String lowerPayee);

  @Query(value = "SELECT PC from PayeeCategorization PC JOIN FETCH PC.subCategory SC JOIN FETCH " +
      "SC.category WHERE PC.payeeId = ?1")
  PayeeCategorization findByIdWithSubCategory(Integer id);

  @Query(value = "SELECT PC from PayeeCategorization PC " +
      "JOIN FETCH PC.subCategory SC " +
      "JOIN FETCH SC.category " +
      "WHERE (:totalEvents IS NULL OR PC.totalEvents >= :totalEvents) " +
      "AND (:subCategoryId IS NULL OR SC.id = :subCategoryId) " +
      "AND (:payee IS NULL OR :payee = '' OR PC.lowerPayee LIKE CONCAT('%', :payee, '%')) " +
      "ORDER BY PC.creationDate DESC")
  Page<PayeeCategorization> findAllWithSubCategory(@Param("totalEvents") Integer totalEvents,
                                                   @Param("subCategoryId") Integer subCategoryId,
                                                   @Param("payee") String payee,
                                                   Pageable pageable);


  @Query(value = "SELECT 1 FROM payee_categorizations WHERE lower_payee = :lowerPayee",
      nativeQuery = true)
  Integer existsByLowerPayee(String lowerPayee);

  @Transactional
  @Modifying
  @Query(value = "UPDATE payee_categorizations SET total_events = total_events + 1, modified_at =" +
      " :modifiedAt  WHERE " +
      "lower_payee = :lowerPayee", nativeQuery = true)
  void updateEventsByLowerPayee(@Param("lowerPayee") String lowerPayee,
                                @Param("modifiedAt") LocalDateTime modifiedAt);

  @Transactional
  @Modifying
  @Query(value = "UPDATE payee_categorizations SET sub_category_id = :subCategoryId WHERE " +
      "payee_id = :payeeId", nativeQuery = true)
  void updateSubCategoryByPayeeId(@Param("subCategoryId") Integer subCategoryId,
                                  @Param("payeeId") Integer payeeId);
}

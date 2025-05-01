package com.bindord.financemanagement.repository;

import com.bindord.financemanagement.model.finance.ExpenditureOthers;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenditureOthersRepository extends JpaRepository<ExpenditureOthers, Integer> {

  @Query(value = "SELECT EX from ExpenditureOthers EX " +
      "JOIN FETCH EX.subCategory SC " +
      "JOIN FETCH SC.category " +
      "WHERE (:subCategoryId IS NULL OR SC.id = :subCategoryId) " +
      "ORDER BY EX.transactionDate DESC")
  Page<ExpenditureOthers> findAllWithSubCategory(@Param("subCategoryId") Integer subCategoryId,
                                           Pageable pageable);

  @Query(value = "SELECT EX from ExpenditureOthers EX JOIN FETCH EX.subCategory SC JOIN FETCH " +
      "SC.category WHERE EX.id = ?1")
  ExpenditureOthers findByIdWithSubCategory(Integer id);

  @Transactional
  @Modifying
  @Query(value = "UPDATE expenditure_others SET sub_category_id = :subCategoryId WHERE " +
      "id = :id", nativeQuery = true)
  void updateSubCategoryById(@Param("subCategoryId") Integer subCategoryId,
                             @Param("id") Integer id);

  @Transactional
  @Modifying
  @Query(value = "UPDATE ExpenditureOthers EX SET EX.wasImported = true WHERE EX.id = ?1")
  void updateImportStateById(Integer id);
}

package com.bindord.financemanagement.repository;

import com.bindord.financemanagement.model.finance.Expenditure;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface ExpenditureRepository extends JpaRepository<Expenditure, Integer> {

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
}

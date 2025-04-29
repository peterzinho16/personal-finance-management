package com.bindord.financemanagement.repository;

import com.bindord.financemanagement.model.finance.Expenditure;
import com.bindord.financemanagement.model.finance.ExpenditureOthers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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
}

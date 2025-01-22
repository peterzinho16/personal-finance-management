package com.bindord.financemanagement.repository;

import com.bindord.financemanagement.model.finance.ExpenditureInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenditureInstallmentRepository extends JpaRepository<ExpenditureInstallment,
    Integer> {

  @Query(value = "SELECT EI from ExpenditureInstallment EI JOIN FETCH EI.subCategory SC JOIN FETCH " +
      "SC.category WHERE EI.fullPaid is False")
  List<ExpenditureInstallment> findAllByFullPaidIsFalse();
}
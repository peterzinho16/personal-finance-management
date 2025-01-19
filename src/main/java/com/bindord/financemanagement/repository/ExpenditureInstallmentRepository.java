package com.bindord.financemanagement.repository;

import com.bindord.financemanagement.model.finance.ExpenditureInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenditureInstallmentRepository extends JpaRepository<ExpenditureInstallment,
    Integer> {
}
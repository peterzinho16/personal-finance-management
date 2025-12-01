package com.bindord.financemanagement.repository;

import com.bindord.financemanagement.model.finance.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Integer> {
}

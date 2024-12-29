package com.bindord.financemanagement.repository;

import com.bindord.financemanagement.model.finance.Expenditure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ExpenditureRepository extends JpaRepository<Expenditure, Integer> {

  @Query(value = "SELECT E.referenceId FROM Expenditure E WHERE E.referenceId IN (?1)")
  Set<String> findByReferenceIdIn(Set<String> refIds);
}

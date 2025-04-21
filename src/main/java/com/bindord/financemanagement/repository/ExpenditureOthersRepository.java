package com.bindord.financemanagement.repository;

import com.bindord.financemanagement.model.finance.ExpenditureOthers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExpenditureOthersRepository extends JpaRepository<ExpenditureOthers, Integer> {

  @Query(value = "SELECT EX FROM ExpenditureOthers EX JOIN FETCH EX.subCategory SC WHERE EX.id = ?1")
  Optional<ExpenditureOthers> findById(Integer id);
}

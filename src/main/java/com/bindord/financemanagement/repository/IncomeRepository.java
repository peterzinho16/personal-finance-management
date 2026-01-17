package com.bindord.financemanagement.repository;

import com.bindord.financemanagement.model.finance.Income;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Integer> {

  @Query(value = "SELECT IC FROM Income IC " +
      "ORDER BY IC.createdAt DESC")
  Page<Income> findAllPageable(Pageable pageable);

  @Transactional
  @Modifying
  @Query(value = "UPDATE incomes SET was_received = :wasReceived, received_date = now() WHERE " +
      "id = :id", nativeQuery = true)
  void updateWasReceivedStatus(@Param("wasReceived") Boolean wasReceived,
                               @Param("id") Integer id);
}

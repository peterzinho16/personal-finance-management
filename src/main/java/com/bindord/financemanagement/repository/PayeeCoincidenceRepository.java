package com.bindord.financemanagement.repository;

import com.bindord.financemanagement.model.finance.PayeeCoincidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayeeCoincidenceRepository extends JpaRepository<PayeeCoincidence, Integer> {


  @Query("SELECT pc FROM PayeeCoincidence pc JOIN FETCH pc.subCategory")
  List<PayeeCoincidence> findAllWithSubCategory();

}

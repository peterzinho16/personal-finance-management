package com.bindord.financemanagement.repository;

import com.bindord.financemanagement.model.finance.RecurrentExpenditure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecurrentExpenditureRepository
    extends JpaRepository<RecurrentExpenditure, Integer> {

  @Query("SELECT re FROM RecurrentExpenditure re JOIN FETCH re.subCategory SC JOIN FETCH SC" +
      ".category C")
  List<RecurrentExpenditure> findAllWithSubCategoryAndCat();

  @Query("SELECT re FROM RecurrentExpenditure re JOIN FETCH re.subCategory SC JOIN FETCH SC" +
      ".category C WHERE re.enabled")
  List<RecurrentExpenditure> findAllEnabledWithSubCategoryAndCat();
}

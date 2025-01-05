package com.bindord.financemanagement.repository;

import com.bindord.financemanagement.model.finance.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategory, Integer> {

    @Query("SELECT sc FROM SubCategory sc JOIN FETCH sc.category WHERE sc.id = ?1")
    SubCategory getById(Integer id);

    @Query("SELECT sc FROM SubCategory sc JOIN FETCH sc.category")
    List<SubCategory> findAll();

    SubCategory findByCategoryIdAndName(Integer categoryId, String name);

    SubCategory findByName(String name);
}

package com.bindord.financemanagement.repository;

import com.bindord.financemanagement.model.finance.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

  Category findByName(String name);

  Category findByNameIgnoreCase(String name);

}

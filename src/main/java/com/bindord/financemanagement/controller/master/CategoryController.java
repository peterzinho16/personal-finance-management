package com.bindord.financemanagement.controller.master;

import com.bindord.financemanagement.model.finance.Category;
import com.bindord.financemanagement.repository.CategoryRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/eureka/finance-app/category")
@AllArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping("")
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }
}

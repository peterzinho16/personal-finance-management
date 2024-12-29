package com.bindord.financemanagement.controller;

import com.bindord.financemanagement.model.finance.SubCategory;
import com.bindord.financemanagement.repository.SubCategoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Controller
@RestController
@RequestMapping("/eureka/finance-app/sub-category")
@AllArgsConstructor
public class SubCategoryController {

    private final SubCategoryRepository subCategoryRepository;

    @GetMapping
    public List<SubCategory> findAll() {
        var list = subCategoryRepository.findAll();
        return list;
    }
    @GetMapping("/{id}")

    public SubCategory getByIdOne(@PathVariable Integer id) {
        var subCat = subCategoryRepository.findById(id).get();
        return subCat;
    }

}

package com.bindord.financemanagement.controller.expend;

import com.bindord.financemanagement.model.finance.Expenditure;
import com.bindord.financemanagement.repository.ExpenditureRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Controller
@RestController
@RequestMapping("/eureka/finance-app/expenditure")
@AllArgsConstructor
public class ExpenditureController {

  private final ExpenditureRepository repository;

  @GetMapping
  Page<Expenditure> findAll(Pageable pageable) {
    return repository.findAll(pageable);
  }

  @GetMapping("/{id}")
  Expenditure findById(@PathVariable Integer id) throws Exception {
    return repository.findById(id).orElseThrow(() -> new Exception("Not found entity"));
  }

  @PostMapping
  Expenditure save(Expenditure expenditure) {
    return repository.save(expenditure);
  }

  @PostMapping("/persist/batch")
  List<Expenditure> save(List<Expenditure> expenditures) {
    return repository.saveAll(expenditures);
  }
}

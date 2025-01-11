package com.bindord.financemanagement.controller.master;

import com.bindord.financemanagement.model.finance.RecurrentExpenditure;
import com.bindord.financemanagement.repository.RecurrentExpenditureRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Controller
@RestController
@RequestMapping("/eureka/finance-app/recurrent-expenditure")
@AllArgsConstructor
public class RecurrentExpenditureController {

  private final RecurrentExpenditureRepository recurrentExpenditureRepository;

  @GetMapping("")
  public List<RecurrentExpenditure> findAll() {
    return recurrentExpenditureRepository.findAllWithSubCategoryAndCat();
  }
}

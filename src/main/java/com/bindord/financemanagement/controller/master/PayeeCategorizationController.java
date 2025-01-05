package com.bindord.financemanagement.controller.master;

import com.bindord.financemanagement.model.finance.PayeeCategorization;
import com.bindord.financemanagement.repository.PayeeCategorizationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Controller
@RestController
@RequestMapping("/eureka/finance-app/payee-categorization")
@AllArgsConstructor
public class PayeeCategorizationController {

  private final PayeeCategorizationRepository payeeCategorizationRepository;

  @GetMapping
  public Page<PayeeCategorization> findAllWithPageable(
      Pageable pageable, @RequestParam(required = false) Integer totalEvents) {
    return payeeCategorizationRepository.findAllWithSubCategory(totalEvents, pageable);
  }

  @GetMapping("/{id}")
  public PayeeCategorization findAllWithPageable(@PathVariable Integer id) {
    return payeeCategorizationRepository.findByIdWithSubCategory(id);
  }
}

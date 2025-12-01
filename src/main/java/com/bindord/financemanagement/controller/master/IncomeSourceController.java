package com.bindord.financemanagement.controller.master;

import com.bindord.financemanagement.utils.enums.IncomeSource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/eureka/finance-app/income-source")
@AllArgsConstructor
public class IncomeSourceController {

  @GetMapping("")
  public List<String> getIncomeSources() {
    return Arrays.stream(IncomeSource.values())
        .map(IncomeSource::getValue)
        .toList();
  }
}
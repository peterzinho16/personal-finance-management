package com.bindord.financemanagement.controller.income;

import com.bindord.financemanagement.advice.CustomValidationException;
import com.bindord.financemanagement.config.AppDataConfiguration;
import com.bindord.financemanagement.model.finance.Income;
import com.bindord.financemanagement.model.finance.IncomeRequestDto;
import com.bindord.financemanagement.repository.IncomeRepository;
import com.bindord.financemanagement.utils.enums.Currency;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/eureka/finance-app/income")
@AllArgsConstructor
public class IncomeController {

  private final IncomeRepository incomeRepository;
  private final AppDataConfiguration appDataConfiguration;

  @GetMapping("")
  public Page<Income> getIncomeSources(Pageable pageable) {
    return incomeRepository.findAllPageable(pageable);
  }

  @PostMapping("")
  public Income save(@Valid @RequestBody IncomeRequestDto incomeRequestDto) {
    if (incomeRequestDto.getCurrency() == Currency.USD) {
      var usdExchangeRate =
          appDataConfiguration.getExchangeRateData()
              .get(AppDataConfiguration.CURRENT_USD_EXCHANGE_RATE).getUsdExchangeRate();
      incomeRequestDto.setConversionToPen(
          incomeRequestDto.getAmount() * usdExchangeRate.doubleValue()
      );
    }

    if(!incomeRequestDto.getWasReceived()) {
      incomeRequestDto.setReceivedDate(null);
    }

    Income income = mapToEntity(incomeRequestDto);
    return incomeRepository.save(income);
  }

  // -------------------------------
  // Mapper (DTO → Entity)
  // -------------------------------
  private Income mapToEntity(IncomeRequestDto dto) {
    Income income = new Income();
    income.setAmount(dto.getAmount());
    income.setSource(dto.getSource()); // storing Spanish values (Option A)
    income.setReceivedDate(dto.getReceivedDate());
    income.setDescription(dto.getDescription());
    income.setCurrency(dto.getCurrency());
    income.setWasReceived(dto.getWasReceived() != null ? dto.getWasReceived() : true);
    income.setConversionToPen(dto.getConversionToPen() != null ? dto.getConversionToPen() : 0.0);
    income.setCreatedAt(LocalDateTime.now());
    return income;
  }


  @PutMapping("/{id}/update/received")
  public Income updateSharedStateById(@PathVariable Integer id) throws Exception {
    Boolean wasReceived = true;
    incomeRepository.updateWasReceivedStatus(wasReceived, id);
    return incomeRepository.findById(id).orElseThrow(
        () ->
            new CustomValidationException("Error, income with id: " + id + " was not found"));
  }
}
package com.bindord.financemanagement.config;

import com.bindord.financemanagement.model.source.ExchangeRateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

  @Autowired
  private AppDataConfiguration appDataConfiguration;

  @ModelAttribute("usdExchangeRate")
  public ExchangeRateDto getUsdExchangeRate() {
    return appDataConfiguration.getExchangeRateData()
        .get(AppDataConfiguration.CURRENT_USD_EXCHANGE_RATE);
  }
}
package com.bindord.financemanagement.model.source;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeRateDto {

  private ExchangeRateSource rateSource;
  private LocalDateTime date;
  private BigDecimal usdExchangeRate;

  public enum ExchangeRateSource {
    FROM_SUNAT_SBS, FROM_LOCAL_DB
  }
}

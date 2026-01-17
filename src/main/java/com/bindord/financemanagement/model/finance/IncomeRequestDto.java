package com.bindord.financemanagement.model.finance;

import com.bindord.financemanagement.utils.enums.Currency;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class IncomeRequestDto {

  @NotNull
  @PositiveOrZero
  private Double amount;

  @NotNull
  @Size(min = 1, max = 128)
  private String source;   // must match Spanish labels

  private LocalDateTime receivedDate;

  @Size(max = 512)
  private String description;

  @NotNull
  private Currency currency;

  private Boolean wasReceived;

  @PositiveOrZero
  private Double conversionToPen;
}

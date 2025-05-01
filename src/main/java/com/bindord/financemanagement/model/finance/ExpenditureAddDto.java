package com.bindord.financemanagement.model.finance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@Valid
public class ExpenditureAddDto {

  @NotBlank
  @Size(max = 255)
  private String description;

  @NotBlank
  @Size(max = 255)
  private String payee;
  @Max(99999)
  private Integer subCategoryId;

  private Boolean shared;

  private Boolean lent;

  @Size(max = 255)
  private String lentTo;

  private Boolean wasBorrowed;

  @Size(max = 255)
  private String borrowedFrom;

  @NotNull
  @Min(-999)
  @Max(99999)
  private Double amount;
  @Size(max = 3)
  private String currency;

  private LocalDateTime transactionDate;

  @Min(1)
  @Max(48)
  @Column
  private Short installments;

  private Boolean forDaughter;

  private Boolean expImported;

  private String referenceId;
}

package com.bindord.financemanagement.model.finance;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
public class ExpenditurePOJO {

  private Integer id;

  private String referenceId;

  private String description;

  private String payee;

  private Boolean recurrent = false;

  private LocalDateTime transactionDate;

  private Expenditure.Currency currency;

  private Double amount;

  private Boolean shared = false;

  private Double sharedAmount;

  private Boolean singlePayment = true;

  private Short installments = 1;

  private Boolean lent = false;

  private String lentTo;

  private Expenditure.LoanState loanState;

  private Double loanAmount;

  public enum LoanState {
    PENDING,
    PAID
  }

  public enum Currency {
    PEN,
    USD
  }
}

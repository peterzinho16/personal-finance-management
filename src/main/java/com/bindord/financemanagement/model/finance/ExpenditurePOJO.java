package com.bindord.financemanagement.model.finance;

import com.bindord.financemanagement.utils.enums.Currency;
import com.bindord.financemanagement.utils.enums.LoanState;
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

  private Currency currency;

  private Double amount;

  private Boolean shared = false;

  private Double sharedAmount;

  private Boolean singlePayment = true;

  private Short installments = 1;

  private Boolean lent = false;

  private String lentTo;

  private LoanState loanState;

  private Double loanAmount;
}

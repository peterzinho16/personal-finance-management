package com.bindord.financemanagement.model.finance;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "expenditure_installments")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenditureInstallment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, length = 1000)
  private String description;

  @Column(length = 1000)
  private String payee;

  @Column(name = "transaction_date", nullable = false)
  private LocalDateTime transactionDate;

  @Column(name = "finish_debt_date", nullable = false)
  private LocalDateTime finishDebtDate;

  @Column(nullable = false)
  private Double amount;

  @Column(name = "installment_amount", nullable = false)
  private Double installmentAmount;

  @Column(columnDefinition = "smallint default 1")
  private Short installments = 1;

  @Column(name = "pending_amount", nullable = false)
  private Double pendingAmount;

  @Column(name = "reference_id", nullable = false, unique = true, length = 255)
  private String referenceId;

  @Column(nullable = false, length = 255)
  @Enumerated(EnumType.STRING)
  private Expenditure.Currency currency;

  @Column(name = "full_paid", columnDefinition = "boolean default false")
  private Boolean fullPaid = false;

  @JsonManagedReference
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sub_category_id", nullable = false)
  private SubCategory subCategory;
}

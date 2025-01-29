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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "expenditures")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Expenditure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String referenceId;

    @NotBlank
    @Size(max = 1000)
    @Column(nullable = false)
    private String description;

    @NotBlank
    @Size(max = 1000)
    @Column(nullable = true)
    private String payee;

    @Column
    private Boolean recurrent = false;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime transactionDate;

    @Column
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Double amount;

    @Column
    private Boolean shared = false;

    @PositiveOrZero
    @Column
    private Double sharedAmount;

    @Column
    private Boolean singlePayment = true;

    @Min(1)
    @Max(48)
    @Column
    private Short installments = 1;

    @Column
    private Boolean lent = false;

    @Size(max = 255)
    @Column
    private String lentTo;

    @Column
    @Enumerated(EnumType.STRING)
    private LoanState loanState;

    @Column
    private Boolean wasBorrowed = false;

    @Size(max = 255)
    @Column
    private String borrowedFrom;

    @Column
    @Enumerated(EnumType.STRING)
    private LoanState borrowedState;

    @Column
    private Integer expenditureInstallmentId;

    @Column
    private Boolean forDaughter = false;

    @PositiveOrZero
    @Column
    private Double loanAmount;

    public enum LoanState {
        PENDING,
        PAID
    }

    public enum Currency {
        PEN,
        USD
    }

    @Column
    private Boolean manualRegister = false;

    @JsonManagedReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_category_id", nullable = false)
    private SubCategory subCategory;
}
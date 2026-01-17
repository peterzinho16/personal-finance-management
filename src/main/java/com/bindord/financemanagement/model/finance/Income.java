package com.bindord.financemanagement.model.finance;

import com.bindord.financemanagement.utils.enums.Currency;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "incomes")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Income {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @NotNull
  @PositiveOrZero
  @Column(nullable = false)
  private Double amount;

  @NotNull
  @Column(nullable = false)
  private String source;

  @Column(nullable = true)
  private LocalDateTime receivedDate;

  @Size(max = 512)
  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column
  private LocalDateTime updatedAt;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Currency currency = Currency.PEN;

  @Column
  private Boolean wasReceived = true;

  @PositiveOrZero
  @Column
  private Double conversionToPen = 0.0;

}
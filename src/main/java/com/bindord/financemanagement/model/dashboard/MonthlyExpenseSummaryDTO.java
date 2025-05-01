package com.bindord.financemanagement.model.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyExpenseSummaryDTO {
  private BigDecimal otrosIngresos;
  private BigDecimal finalTotalGastos;
  private String periodo;
  private BigDecimal gastosIndividuales;
  private BigDecimal gastosCompartidos;
  private BigDecimal misGastosPagadosPorTercero;
  private BigDecimal misGastosImportados;
  private BigDecimal totalTusPrestamos;
  private Double gastosRecurrentesTotal;
}


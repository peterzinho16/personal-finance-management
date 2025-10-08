package com.bindord.financemanagement.model.resume;

public interface ResumeSummaryProjection {
  String getPeriodo();
  Double getOtrosIngresos();
  Double getFinalTotalGastos();
  Double getGastosCompartidos();
  Double getMisGastosCompartidosImportados();
  Double getGastosADevolver();
}
package com.bindord.financemanagement.model.dashboard;

import java.math.BigDecimal;

public interface CategoryMonthlyTotalsProjection {
  String getPeriodo();
  String getCategoria();
  BigDecimal getGastosIndividuales();
  BigDecimal getGastosCompartidos();
}

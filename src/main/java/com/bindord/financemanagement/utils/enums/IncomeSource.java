package com.bindord.financemanagement.utils.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum IncomeSource {

  SALARIO_NETO("Salario neto"),
  FREELANCE("Freelance"),
  CTS("CTS"),
  GRATIFICACION("Gratificacion"),
  BONUS("Bonus"),
  UTILIDADES("Utilidades"),
  MONTO_DE_PAGO_COMPARTIDO("Monto de pago compartido"),
  OTRO("Otro");

  private final String value;

  IncomeSource(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }

  public static IncomeSource fromValue(String value) {
    for (IncomeSource s : values()) {
      if (s.value.equalsIgnoreCase(value)) {
        return s;
      }
    }
    throw new IllegalArgumentException("Unknown income source: " + value);
  }
}
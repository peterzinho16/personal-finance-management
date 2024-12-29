package com.bindord.financemanagement.utils;

import com.bindord.financemanagement.model.finance.Expenditure;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MailRegex {

  private static final Pattern EXTRACT_EXPENDITURE_AMOUNT = Pattern.compile("(S/|\\$)\\s*(\\d+(\\.\\d+)?)");
  private static final Pattern VALIDATE_EXPENDITURE_CURRENCY = Pattern.compile("(S/|\\$)");


  public static Double extractExpenditureAmount(String content) {
    Matcher matcher = EXTRACT_EXPENDITURE_AMOUNT.matcher(content);
    if (!matcher.find()) {
      return 0.0;
    }
    return Double.parseDouble(matcher.group(2));
  }

  public static Expenditure.Currency extractExpenditureCurrency(String content) {
    Matcher matcher = VALIDATE_EXPENDITURE_CURRENCY.matcher(content);
    if (!matcher.find()) {
      return null;
    }
    return switch (matcher.group()) {
      case "S/" -> Expenditure.Currency.PEN;
      default -> Expenditure.Currency.USD;
    };
  }
}

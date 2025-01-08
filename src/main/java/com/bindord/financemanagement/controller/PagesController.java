package com.bindord.financemanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PagesController {

  @RequestMapping({"/home"})
  public String showAdminPage() {
    return "expenditure";
  }

  @RequestMapping({"/payee-categorization"})
  public String showPayeeCategorizationPage() {
    return "payee-categorization";
  }

  @RequestMapping({"/bar-chart-categories"})
  public String showBarChartExpendituresCategoriesPage() {
    return "bar-chart-categories";
  }
}

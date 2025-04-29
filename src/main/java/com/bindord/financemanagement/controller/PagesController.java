package com.bindord.financemanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PagesController {

  @RequestMapping({"/home"})
  public String showAdminPage() {
    return "pages/expenditure";
  }

  @RequestMapping({"/home/others"})
  public String showOthersExpenditurePage() {
    return "pages/expenditure-others";
  }

  @RequestMapping({"/payee-categorization"})
  public String showPayeeCategorizationPage() {
    return "pages/payee-categorization";
  }

  @RequestMapping({"/charts/bar-chart-categories"})
  public String showBarChartExpendituresCategoriesPage() {
    return "pages/bar-chart-categories";
  }

  @RequestMapping({"/expenditure/new"})
  public String addNewExpenditureManually() {
    return "pages/expenditure-new";
  }
}

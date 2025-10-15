package com.bindord.financemanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PagesController {

  @RequestMapping({"/expenditure/list"})
  public String showExpenditureListPage() {
    return "pages/expenditure";
  }

  @RequestMapping({"/expenditure/list/others"})
  public String showExpenditureOthersListPage() {
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

  @RequestMapping("/resume")
  public String resumePage() {
    return "pages/resume";
  }
}

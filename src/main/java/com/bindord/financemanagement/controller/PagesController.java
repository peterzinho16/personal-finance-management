package com.bindord.financemanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PagesController {

  @RequestMapping({"/admin"})
  public String showAdminPage() {
    return "basic-admin-template";
  }

  @RequestMapping({"/payee-categorization"})
  public String showPayeeCategorizationPage() {
    return "payee-categorization";
  }
}

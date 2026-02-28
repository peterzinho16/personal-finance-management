package com.bindord.financemanagement;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ErrorPageController {

  @GetMapping("/error-page")
  public String showErrorPage(
      @RequestParam(required = false) String reason,
      Model model) {

    String message = switch (reason) {
      case "invalid-token" -> "The confirmation link is invalid or has expired.";
      case "account-disabled" -> "Your account is disabled.";
      default -> "Something went wrong. Please try again.";
    };

    model.addAttribute("errorMessage", message);
    return "pages/error";
  }
}
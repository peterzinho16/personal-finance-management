package com.bindord.financemanagement.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthenticationController {

  @GetMapping({"/login"})
  public String showLoginPage() {
    return "pages/login";
  }

  @GetMapping({"/register"})
  public String showRegisterPage() {
    return "pages/register";
  }

  @GetMapping({"/registration-confirmation"})
  public String showRegistrationConfirmationPage() {
    return "pages/registration-confirmation";
  }

  @GetMapping({"/activation-success"})
  public String showActivationSuccessPage() {
    return "pages/activation-success";
  }

  @GetMapping({"/activation-error"})
  public String showActivationErrorPage() {
    return "pages/activation-error";
  }

  @PostMapping("/logout")
  public String logout(HttpServletRequest request, HttpServletResponse response) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null) {
      new SecurityContextLogoutHandler().logout(request, response, auth);
    }
    return "redirect:/login?logout";
  }
}

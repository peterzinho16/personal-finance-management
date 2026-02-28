package com.bindord.financemanagement.controller;

import com.bindord.financemanagement.svc.AccountActivationService;
import com.bindord.financemanagement.svc.AccountCodeConfirmationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AccountActivationController {

  private final AccountActivationService activationService;
  private final AccountCodeConfirmationService accountCodeConfirmationService;
  private final PasswordEncoder passwordEncoder;

  @GetMapping("/activate")
  public String activate(@RequestParam String token) {
    try {
      activationService.activateAccount(token);
      return "redirect:/activation-success";
    } catch (IllegalStateException ex) {
      log.warn("Activation failed: {}", ex.getMessage());
      return "redirect:/activation-error";
    }
  }

  @GetMapping("/reset-password")
  public String showResetPassword(
      @RequestParam String token,
      Model model) {

    if (!accountCodeConfirmationService.validateIfTokenWasNotUsed(token)) {
      return "redirect:/error-page?reason=invalid-token";
    }

    model.addAttribute("token", token);
    return "pages/reset-password";
  }

  @PostMapping("/reset-password")
  public String processResetPassword(
      @RequestParam String token,
      @RequestParam String password,
      @RequestParam String confirmPassword,
      RedirectAttributes redirectAttributes,
      Model model) {

    if (!password.equals(confirmPassword)) {
      model.addAttribute("token", token);
      model.addAttribute("error", "Passwords do not match");
      return "pages/reset-password";
    }
    String encoded = passwordEncoder.encode(password);
    activationService.resetPassword(token, encoded);

    return "redirect:/login?resetSuccess";
  }
}
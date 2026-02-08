package com.bindord.financemanagement.controller;

import com.bindord.financemanagement.svc.AccountActivationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AccountActivationController {

  private final AccountActivationService activationService;

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
}
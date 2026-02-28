package com.bindord.financemanagement.controller.auth;

import com.bindord.financemanagement.svc.AccountCodeConfirmationService;
import com.bindord.financemanagement.svc.UserService;
import com.bindord.financemanagement.utils.enums.MailNotificationType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequestMapping("")
@AllArgsConstructor
public class UserController {

  private final UserService userService;
  private final AccountCodeConfirmationService accountCodeConfirmationService;

  @PostMapping("/register")
  public String registerUser(
      @RequestParam String username,
      @RequestParam String password
  ) {
    log.info("Registering user {}", username);
    userService.registerUser(username, password);
    return "redirect:/registration-confirmation";
  }

  @PostMapping("/forgot-password")
  public String processForgotPassword(@RequestParam String email, Model model) {
    // TODO: generate token + send email
    model.addAttribute("message", "If the email exists, a reset link has been sent.");
    if(!userService.validateIfUserExistsAndAccountIsActive(email)) {
      return "pages/forgot-password";
    }
    accountCodeConfirmationService.generateConfirmationCode(email,
        MailNotificationType.RESET_PASSWORD);
    return "pages/forgot-password";
  }
}
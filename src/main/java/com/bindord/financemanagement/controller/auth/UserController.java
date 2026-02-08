package com.bindord.financemanagement.controller.auth;

import com.bindord.financemanagement.svc.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequestMapping("")
@AllArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping("/register")
  public String registerUser(
      @RequestParam String username,
      @RequestParam String password
  ) {
    log.info("Registering user {}", username);
    userService.registerUser(username, password);
    return "redirect:/registration-confirmation";
  }
}
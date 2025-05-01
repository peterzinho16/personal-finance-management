package com.bindord.financemanagement.controller;

import ch.qos.logback.core.model.Model;
import com.bindord.financemanagement.config.AppDataConfiguration;
import com.bindord.financemanagement.model.finance.MicrosoftAccessToken;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.bindord.financemanagement.utils.Utilities.storeSessionToken;
import static com.bindord.financemanagement.utils.Utilities.validateIfExistsValidSession;

@Controller
@RequestMapping("/")
@AllArgsConstructor
@Slf4j
public class WelcomeController {

  private final AppDataConfiguration appDataConfiguration;

  @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
  public String welcomeInitPage(HttpSession session, Model model) {
    MicrosoftAccessToken appData =
        appDataConfiguration.getConfigData().get(AppDataConfiguration.APP_DATA_KEY);

    if (Objects.nonNull(appData) && appData.getExpiresAt().isAfter(LocalDateTime.now())) {
      log.debug("appData {}", appData);
      storeSessionToken(session, appData);
    }

    if (validateIfExistsValidSession(session)) {
      return "pages/welcome-session";  // maps to templates/welcome-session.html
    }

    return "pages/welcome-init";  // maps to templates/welcome-init.html
  }
}
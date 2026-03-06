package com.bindord.financemanagement.controller.expend;

import com.bindord.financemanagement.config.AppDataConfiguration;
import com.bindord.financemanagement.model.finance.MicrosoftAccessToken;
import com.bindord.financemanagement.svc.ExpenditureSyncService;
import com.bindord.financemanagement.svc.GmailFacade;
import com.bindord.financemanagement.utils.enums.MailProvider;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.services.gmail.Gmail;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.bindord.financemanagement.utils.Utilities.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/eureka/finance-app/expenditure/sync")
public class ExpenditureSyncController {

  private final AppDataConfiguration appDataConfiguration;
  private final ExpenditureSyncService expenditureSyncService;
  private final GmailFacade gmailFacade;
  private final GoogleAuthorizationCodeFlow flow;

  @GetMapping
  public String executeSync(@RequestParam MailProvider provider, HttpSession session) throws Exception {

    if (provider == MailProvider.GMAIL) {
      Gmail service = gmailFacade.getGmailService();

      if (service == null
          || flow.loadCredential("user") == null
          || flow.loadCredential("user").getAccessToken() == null
          || flow.loadCredential("user").getExpirationTimeMilliseconds() < System.currentTimeMillis()) {
        return "<a href='/'>Go home page, something went wrong with the session</a>";
      }

      return expenditureSyncService.executeSynchronization(null, MailProvider.GMAIL);
    }

    if (provider == MailProvider.OUTLOOK) {
      MicrosoftAccessToken appData =
          appDataConfiguration.getConfigData().get(AppDataConfiguration.APP_DATA_KEY);

      if (Objects.nonNull(appData) && appData.getExpiresAt().isAfter(LocalDateTime.now())) {
        log.debug("appData {}", appData);
        storeSessionToken(session, appData);
      }

      if (validateIfExistsValidSession(session)) {
        return expenditureSyncService.executeSynchronization(
            retrieveSessionToken(session).getAccessToken(),
            MailProvider.OUTLOOK);
      }

      return "No valid session to proceed";
    }

    return "Unsupported mail provider";
  }
}

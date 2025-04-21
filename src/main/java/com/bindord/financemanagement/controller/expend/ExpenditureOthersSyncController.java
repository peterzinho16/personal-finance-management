package com.bindord.financemanagement.controller.expend;

import com.bindord.financemanagement.svc.ExpenditureOthersSyncService;
import com.bindord.financemanagement.svc.GmailFacade;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.services.gmail.Gmail;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Controller
@RestController
@AllArgsConstructor
@RequestMapping("/eureka/finance-app/expenditure-others/sync")
public class ExpenditureOthersSyncController {


  private final ExpenditureOthersSyncService expenditureSyncService;
  private final GmailFacade gmailFacade;
  private final GoogleAuthorizationCodeFlow flow;


  @GetMapping
  public String executeSync() throws Exception {
    Gmail service = gmailFacade.getGmailService();
    if (service == null || flow.loadCredential("user") == null
        || flow.loadCredential("user").getAccessToken() == null
        || flow.loadCredential("user").getExpirationTimeMilliseconds() < System.currentTimeMillis()) {
      return "<a href='/'>Go home page, something went wrong with the session</a>";
    }
    return expenditureSyncService.executeSynchronization();
  }
}

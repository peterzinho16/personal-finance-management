package com.bindord.financemanagement.controller.expend;

import com.bindord.financemanagement.config.AppDataConfiguration;
import com.bindord.financemanagement.model.finance.MicrosoftAccessToken;
import com.bindord.financemanagement.svc.ExpenditureSyncService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

import static com.bindord.financemanagement.utils.Utilities.*;

@Controller
@RestController
@AllArgsConstructor
@RequestMapping("/eureka/finance-app/expenditure/sync")
public class ExpenditureSyncController {


  private final AppDataConfiguration appDataConfiguration;
  private final ExpenditureSyncService expenditureSyncService;

  @GetMapping
  public String executeSync(HttpSession session) throws Exception {

    MicrosoftAccessToken appData =
        appDataConfiguration.getConfigData().get(AppDataConfiguration.APP_DATA_KEY);
    if (Objects.nonNull(appData)) {
      storeSessionToken(session, appData);
    }
    if (validateIfExistsValidSession(session)) {
      return expenditureSyncService.executeSynchronization(retrieveSessionToken(session).getAccessToken());
    }
    return "No valid session to proceed";
  }
}

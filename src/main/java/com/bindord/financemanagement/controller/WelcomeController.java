package com.bindord.financemanagement.controller;

import com.bindord.financemanagement.config.AppDataConfiguration;
import com.bindord.financemanagement.model.finance.MicrosoftAccessToken;
import com.bindord.financemanagement.utils.Utilities;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

import static com.bindord.financemanagement.utils.Utilities.storeSessionToken;
import static com.bindord.financemanagement.utils.Utilities.validateIfExistsValidSession;

@RestController
@RequestMapping("/")
@AllArgsConstructor
public class WelcomeController {

  private final AppDataConfiguration appDataConfiguration;

  /**
   * Welcome init page.</br>
   * <b>Validate if:</b></br>
   * 1.0 Exists a valid JWT in DB </br>
   * 1.1 If exists store in HttpSession</br>
   * 2.0 If HttpSession exists and check its validity</br>
   * 2.1 If not valid, shows init flow link
   * 3.0 If HttpSession valid, shows it as a message
   *
   * @param session the session
   * @return the view to init the flow
   */
  @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
  public String welcomeInitPage(HttpSession session) {
    MicrosoftAccessToken appData = appDataConfiguration.getConfigData().get(AppDataConfiguration.APP_DATA_KEY);
    if (Objects.nonNull(appData)) {
      storeSessionToken(session, appData);
    }
    if (validateIfExistsValidSession(session)) {
      return """
          <html>
             <head></head>
             <body>
                <div style="text-align: center"><a href="#">There is a session already</a></div>
                <div style="text-align: center"><a href="/eureka/finance-app/api-graph/get-messages">😁 Go direct messages</a></div>
                <div style="text-align: center"><a href="/eureka/finance-app/automatic-ingest/full">😎 Go full ingest</a></div>
             </body>
          </html>
          """;
    }
    return """
        <html>
           <head></head>
           <body>
              <div style="text-align: center"><a href="/eureka/finance-app/api-graph/start-flow">Init Flow</a></div>
           </body>
        </html>
        """;
  }


}

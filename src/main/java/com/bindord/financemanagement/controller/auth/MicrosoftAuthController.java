package com.bindord.financemanagement.controller.auth;

import com.bindord.financemanagement.config.JacksonFactory;
import com.bindord.financemanagement.model.finance.MicrosoftAccessToken;
import com.bindord.financemanagement.model.oauth.MicrosoftAccessTokenRecord;
import com.bindord.financemanagement.repository.MicrosoftAccessTokenRepository;
import com.bindord.financemanagement.repository.external.MicrosoftOauthClient;
import com.bindord.financemanagement.utils.Utilities;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Controller
@RestController
@RequestMapping("/eureka/finance-app/api-graph")
@AllArgsConstructor
public class MicrosoftAuthController {

  private final MicrosoftAccessTokenRepository microsoftAccessTokenRepository;
  private final MicrosoftOauthClient microsoftOauthRepository;

  @GetMapping("/start-flow")
  public RedirectView startFlow(HttpSession session) {
    log.info("Starting flow...");
    RedirectView redirectView = new RedirectView();

    if (Utilities.validateIfExistsValidSession(session)) {
      log.info("Exists a valid session already");
      redirectView.setUrl("/");
      return redirectView;
    }
    // 1. Call your internal GET REST controller logic (via service)
    String redirectMicrosoftURL = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize?" +
        "client_id=" + System.getenv("APP_CLIENT_ID") +
        "&response_type=code" +
        "&redirect_uri=http://localhost:8080/eureka/finance-app/api-graph/exchange-code" +
        "&scope=Mail.Read" +
        "&state=" + UUID.randomUUID();

    // 3. Create a RedirectView
    redirectView.setUrl(redirectMicrosoftURL);
    return redirectView;
  }

  @GetMapping(value = "/exchange-code", produces = MediaType.TEXT_HTML_VALUE)
  public String exchangeApiGraphToken(@RequestParam String code, @RequestParam String state, HttpSession session) throws JsonProcessingException {
    log.info("code: {}", code);
    log.info("state: {}", state);
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

    params.add("client_id", System.getenv("APP_CLIENT_ID"));
    params.add("client_secret", System.getenv("APP_CC_CLIENT_SECRET"));
    params.add("code", code);
    params.add("grant_type", "authorization_code");
    params.add("redirect_uri", "http://localhost:8080/eureka/finance-app/api-graph/exchange-code");
    params.add("scope", "Mail.Read");

    MicrosoftAccessTokenRecord tokenRecord = microsoftOauthRepository.getAccessToken(params);
    MicrosoftAccessToken accessTokenToDb = MicrosoftAccessToken.builder().build();
    BeanUtils.copyProperties(tokenRecord, accessTokenToDb);
    var now = LocalDateTime.now();
    var expiresAt = now.plusSeconds(tokenRecord.expiresIn());
    accessTokenToDb.setCreatedAt(LocalDateTime.now());
    accessTokenToDb.setExpiresAt(expiresAt);

    session.setAttribute("sessionToken", accessTokenToDb);

    var response = JacksonFactory
        .getObjectMapper()
        .writeValueAsString(microsoftAccessTokenRepository.save(accessTokenToDb));

    return String.format("""
        <html>
            <head></head>
            <body>
                <div style="text-align: center">
                    <a href="/">Go to options</a>
                </div>
                <br>
                <h4 style="text-align: center">Response</h4>
                <div style="text-align: center">
                    <pre style="white-space: pre-wrap; word-wrap: break-word;">
                        <code>%s</code>
                    </pre>
                </div>
            </body>
        </html>
        """, response);
  }


}

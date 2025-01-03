package com.bindord.financemanagement.controller;

import com.bindord.financemanagement.model.finance.MicrosoftAccessToken;
import com.bindord.financemanagement.model.oauth.MicrosoftAccessTokenRecord;
import com.bindord.financemanagement.model.source.MailMessagesResponse;
import com.bindord.financemanagement.repository.MicrosoftAccessTokenRepository;
import com.bindord.financemanagement.repository.external.MicrosoftGraphClient;
import com.bindord.financemanagement.repository.external.MicrosoftOauthClient;
import com.bindord.financemanagement.utils.Constants;
import com.bindord.financemanagement.utils.Utilities;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Controller
@RestController
@RequestMapping("/eureka/finance-app/api-graph")
@AllArgsConstructor
public class MicrosoftApiGraphController {

  private final MicrosoftAccessTokenRepository microsoftAccessTokenRepository;
  private final MicrosoftOauthClient microsoftOauthRepository;
  private final MicrosoftGraphClient microsoftGraphClient;


  @GetMapping("/exchange-code")
  public MicrosoftAccessToken exchangeApiGraphToken(@RequestParam String code, @RequestParam String state, HttpSession session) {

    System.out.println("code: " + code);
    System.out.println("state: " + state);
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("client_id", "b33f4746-8cfc-41d4-a6bc-b13c92980040");
    params.add("client_secret", "pG28Q~W~TnLlqsSGMRadwkQYSA4fxYICXBVIzdpW");
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
    return microsoftAccessTokenRepository.save(accessTokenToDb);
  }


  @GetMapping("/get-messages")
  public ResponseEntity<MailMessagesResponse> retrieveContentFromApiGraph(HttpSession session) {
    if (!Utilities.validateIfExistsValidSession(session)) {
      log.info("There is no session or a valid session");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    MicrosoftAccessToken token = (MicrosoftAccessToken) session.getAttribute(Utilities.SESSION_TOKEN);
    MailMessagesResponse response = microsoftGraphClient.getMessages(Constants.INBOX_FOLDER_ID, Constants.NOTIF_COMPRAS_SUB_FOLDER_ID, 10, 35, token.getAccessToken());
    log.info("Records: {}", Objects.requireNonNull(response).getValue().size());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/start-flow")
  public RedirectView startFlow(HttpSession session) {
    RedirectView redirectView = new RedirectView();

    if (Utilities.validateIfExistsValidSession(session)) {
      log.info("Exists a valid session already");
      redirectView.setUrl("/");
      return redirectView;
    }
    // 1. Call your internal GET REST controller logic (via service)
    String redirectMicrosoftURL = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize?client_id=b33f4746-8cfc-41d4-a6bc-b13c92980040&response_type=code&redirect_uri=http://localhost:8080/eureka/finance-app/api-graph/exchange-code&scope=Mail.Read&state=3f94aee0-ebc6-4dc3-b89a-52a60380acd4";

    // 3. Create a RedirectView
    redirectView.setUrl(redirectMicrosoftURL);
    return redirectView;

    //Or you can use ResponseEntity
    //return ResponseEntity.status(HttpStatus.FOUND).header("Location", redirectUrl).build();
  }


}

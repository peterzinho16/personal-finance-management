package com.bindord.financemanagement.controller.auth;


import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RequestMapping("/eureka/finance-app/api-google")
@RestController
public class GoogleAuthController {

  private static final Logger logger = LoggerFactory.getLogger(GoogleAuthController.class);

  private final GoogleAuthorizationCodeFlow flow;

  @Value("${spring.application.name}")
  private String applicationName;

  @Value("${gmail.oauth.redirect.uri}")
  private String redirectUri;

  public GoogleAuthController(GoogleAuthorizationCodeFlow flow) {
    this.flow = flow;
  }

  @GetMapping("/start-flow")
  public RedirectView authorize() throws IOException {
    AuthorizationCodeRequestUrl authorizationUrl = flow.newAuthorizationUrl();
    authorizationUrl.setRedirectUri(redirectUri);
    String url = authorizationUrl.build();
    logger.info("Redirecting user to Google for authorization: {}", url);
    return new RedirectView(url);
  }

  @GetMapping("/exchange-code")
  public String oauth2Callback(@RequestParam(value = "code") String code) throws IOException, GeneralSecurityException {
    logger.info("Received authorization code: {}", code);
    if (code != null) {
      TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectUri).execute();
      flow.createAndStoreCredential(response, "user"); // "user" is a user identifier
      return "Authorization successful! You can now access your Gmail.";
    } else {
      return "Authorization failed.";
    }
  }
}
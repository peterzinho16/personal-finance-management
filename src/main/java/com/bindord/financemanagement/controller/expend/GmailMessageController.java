package com.bindord.financemanagement.controller.expend;

import com.bindord.financemanagement.HTMLTextExtractor;
import com.bindord.financemanagement.model.source.GmailMessageDto;
import com.bindord.financemanagement.svc.GmailFacade;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.gmail.Gmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.List;


@Controller
public class GmailMessageController {

  private static final Logger logger = LoggerFactory.getLogger(GmailMessageController.class);
  private static final String APPLICATION_NAME = "Gmail API Spring Boot";

  private final GoogleAuthorizationCodeFlow flow;
  private final JsonFactory jsonFactory;
  private final GmailFacade gmailFacade;

  public GmailMessageController(GoogleAuthorizationCodeFlow flow, JsonFactory jsonFactory,
                                GmailFacade gmailFacade) {
    this.flow = flow;
    this.jsonFactory = jsonFactory;
    this.gmailFacade = gmailFacade;
  }

  private Gmail getGmailService() throws IOException, GeneralSecurityException {
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    return new Gmail.Builder(HTTP_TRANSPORT, jsonFactory, flow.loadCredential("user"))
        .setApplicationName(APPLICATION_NAME)
        .build();
  }

  @GetMapping("/gmail/list")
  @ResponseBody
  public String listInboxMessages() throws IOException, GeneralSecurityException {
    Gmail service = getGmailService();
    if (service == null || flow.loadCredential("user") == null
        || flow.loadCredential("user").getAccessToken() == null
        || flow.loadCredential("user").getExpirationTimeMilliseconds() < System.currentTimeMillis()) {
      return "<a href='/eureka/finance-app/api-google/gmail/auth'>Authorize Gmail Access</a>";
    }

    List<GmailMessageDto> gmailMessages;
    try {
      gmailMessages = gmailFacade.findByCreatedDateTimeGreaterThan(LocalDateTime.now().minusDays(30).plusSeconds(1));
    } catch (GeneralSecurityException | IOException e) {
      logger.error("Error fetching Gmail messages: {}", e.getMessage(), e);
      return "Error fetching Gmail messages. Check logs."; // Or return an error page
    }

    StringBuilder result = new StringBuilder("<h3>Inbox Messages:</h3><ul>");

    if (gmailMessages.isEmpty()) {
      result.append("<li>No messages found.</li>");
    } else {
      for (GmailMessageDto message : gmailMessages) {

        result.append("<li><strong>ID:</strong> ").append(message.getId())
            .append("<br><strong>Subject:</strong> ").append(message.getSubject())
            .append("<br><strong>Snippet:</strong> ").append(message.getBodyPreview())
            .append("<br><strong>Date:</strong> ").append(message.getCreatedDateTime())
            .append("<br><strong>Link:</strong>").append("https://mail.google.com/mail/u/0/#inbox/"+message.getWebLink())
            .append("<br><strong>HTML only text:</strong> ").append(HTMLTextExtractor.extractTextJsoup(message.getBodyHTML()))
            .append("</li><br>");
      }
    }
    result.append("</ul>");
    return result.toString();
  }
}
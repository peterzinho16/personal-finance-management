package com.bindord.financemanagement.controller.expend;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Controller
public class GmailMessageController {

  private static final Logger logger = LoggerFactory.getLogger(GmailMessageController.class);
  private static final String APPLICATION_NAME = "Gmail API Spring Boot";
  private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss z"; // You can adjust the format

  private final GoogleAuthorizationCodeFlow flow;
  private final JsonFactory jsonFactory;

  public GmailMessageController(GoogleAuthorizationCodeFlow flow, JsonFactory jsonFactory) {
    this.flow = flow;
    this.jsonFactory = jsonFactory;
  }

  private Gmail getGmailService() throws IOException, GeneralSecurityException {
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    Credential credential = flow.loadCredential("user");
    if (credential != null && credential.getRefreshToken() != null) {
      return new Gmail.Builder(HTTP_TRANSPORT, jsonFactory, credential)
          .setApplicationName(APPLICATION_NAME)
          .build();
    } else {
      logger.warn("No valid stored credential found. User needs to authorize.");
      return null;
    }
  }

  @GetMapping("/gmail/list")
  @ResponseBody
  public String listInboxMessages() throws IOException, GeneralSecurityException {
    Gmail service = getGmailService();
    if (service == null) {
      return "<a href='/eureka/finance-app/api-google/gmail/auth'>Authorize Gmail Access</a>";
    }

    // List the first 10 messages in the inbox
    Gmail.Users.Messages.List listRequest = service.users().messages().list("me")
        .setMaxResults(10L)
        .setLabelIds(Collections.singletonList("INBOX"));

    // Execute the list request and get the raw HTTP response
    HttpResponse listResponse = listRequest.executeUnparsed();
    String rawListJson = listResponse.parseAsString();
    logger.debug("Raw JSON response for message list: {}", rawListJson);

    ListMessagesResponse messagesResponse = listRequest.execute();
    List<Message> messages = messagesResponse.getMessages();
    StringBuilder result = new StringBuilder("<h3>Inbox Messages:</h3><ul>");

    if (messages == null || messages.isEmpty()) {
      result.append("<li>No messages found.</li>");
    } else {
      SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

      for (Message message : messages) {
        Gmail.Users.Messages.Get getRequest = service.users().messages().get("me", message.getId());

        // Execute the get request and get the raw HTTP response for each message
        HttpResponse getResponse = getRequest.executeUnparsed();
        String rawGetJson = getResponse.parseAsString();
        logger.debug("Raw JSON response for message ID {}: {}", message.getId(), rawGetJson);

        com.google.api.services.gmail.model.Message fullMessage = getRequest.execute();

        String subject = "";
        for (MessagePartHeader header : fullMessage.getPayload().getHeaders()) {
          if (header.getName().equalsIgnoreCase("Subject")) {
            subject = header.getValue();
            break;
          }
        }

        Long internalDate = fullMessage.getInternalDate();
        Date receivedDate = new Date(internalDate);
        String formattedDate = sdf.format(receivedDate);

        result.append("<li><strong>ID:</strong> ").append(message.getId())
            .append("<br><strong>Subject:</strong> ").append(subject)
            .append("<br><strong>Snippet:</strong> ").append(fullMessage.getSnippet())
            .append("<br><strong>Date:</strong> ").append(formattedDate)
            .append("</li><br>");
      }
    }
    result.append("</ul>");
    return result.toString();
  }
}
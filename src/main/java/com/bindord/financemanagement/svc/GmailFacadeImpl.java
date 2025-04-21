package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.model.mapper.GmailMapper;
import com.bindord.financemanagement.model.source.GmailMessageDto;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class GmailFacadeImpl implements GmailFacade {

  private static final String APPLICATION_NAME = "Your Application Name";

  private final GoogleAuthorizationCodeFlow flow;
  private final JsonFactory jsonFactory;

  @Override
  public Gmail getGmailService() throws IOException, GeneralSecurityException {
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    return new Gmail.Builder(HTTP_TRANSPORT, jsonFactory, flow.loadCredential("user"))
        .setApplicationName(APPLICATION_NAME)
        .build();
  }

  @Override
  public List<GmailMessageDto> findByCreatedDateTimeGreaterThan(LocalDateTime createdDateTime)
      throws GeneralSecurityException, IOException {
    Gmail service = getGmailService();
    if (service == null) {
      log.warn("No valid stored credential found. User needs to authorize.");
      return Collections.emptyList(); // Or throw a custom exception
    }

    List<GmailMessageDto> gmailMessages = new ArrayList<>();
    String query = "after:" + createdDateTime.toLocalDate();
    String labelName = "Notificaciones Compras"; // Store the label name
    String labelId = getLabelId(service, labelName); // Get the label ID

    if (labelId == null) {
      log.error("Label '{}' not found. Cannot proceed.", labelName);
      return Collections.emptyList(); // Or throw a custom exception
    }

    Gmail.Users.Messages.List listRequest = service.users().messages().list("me")
        .setLabelIds(Collections.singletonList(labelId)) // Use the label ID!
        .setQ(query);

    ListMessagesResponse messagesResponse;
    List<Message> messages;
    String nextPageToken = null;

    do {
      listRequest.setPageToken(nextPageToken);
      messagesResponse = listRequest.execute();
      messages = messagesResponse.getMessages();

      if (messages != null && !messages.isEmpty()) {
        for (Message message : messages) {
          Gmail.Users.Messages.Get getRequest = service.users().messages().get("me",
                  message.getId())
              .setFormat("FULL"); // Consider using FULL for all data
          Message fullMessage = getRequest.execute();
          try {
            LocalDateTime messageDateTime = LocalDateTime.ofEpochSecond(
                fullMessage.getInternalDate() / 1000, 0, java.time.ZoneOffset.UTC);
            if (messageDateTime.isAfter(createdDateTime)) {
              gmailMessages.add(GmailMapper.mapToGmailMessageDto(fullMessage));
            }
          } catch (IOException e) {
            log.error("Error mapping message with ID: {}", message.getId(), e);
            // Handle mapping error - skip, or rethrow as a custom exception
          }
        }
      }
      nextPageToken = messagesResponse.getNextPageToken();
    } while (nextPageToken != null);

    return gmailMessages.reversed();
  }

  // Helper method to get the label ID from the label name
  private String getLabelId(Gmail service, String labelName) throws IOException {
    Gmail.Users.Labels.List labelRequest = service.users().labels().list("me");
    ListLabelsResponse labelResponse = labelRequest.execute();
    List<com.google.api.services.gmail.model.Label> labels = labelResponse.getLabels();

    if (labels != null) {
      for (com.google.api.services.gmail.model.Label label : labels) {
        if (label.getName().equalsIgnoreCase(labelName)) {
          return label.getId();
        }
      }
    }
    return null; // Label not found
  }
}
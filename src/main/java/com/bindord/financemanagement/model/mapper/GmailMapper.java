package com.bindord.financemanagement.model.mapper;

import com.bindord.financemanagement.model.source.GmailMessageDto;
import com.bindord.financemanagement.utils.Utilities;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

public class GmailMapper {

  public static final String BASE_PATH_FOR_WEB_LINK_GMAIL = "https://mail.google" +
      ".com/mail/u/0/#inbox/";

  public static GmailMessageDto mapToGmailMessageDto(Message fullMessage) throws IOException {
    GmailMessageDto dto = new GmailMessageDto();

    dto.setId(fullMessage.getId());
    dto.setThreadId(fullMessage.getThreadId());
    dto.setWebLink(BASE_PATH_FOR_WEB_LINK_GMAIL + fullMessage.getId()); // Or construct a proper
    // web link if needed
    dto.setCreatedDateTime(parseDate(fullMessage.getInternalDate()));
    dto.setSubject(getHeaderValue(fullMessage.getPayload().getHeaders(), "Subject"));
    dto.setFrom(getHeaderValue(fullMessage.getPayload().getHeaders(), "From"));
    dto.setBodyPreview(getBodyPreview(fullMessage));
    dto.setBodyHTML(getHtmlContent(fullMessage));

    return dto;
  }

  private static String parseDate(Long internalDate) {
    if (internalDate == null) {
      return null; // Or handle the null case as appropriate
    }
    Instant instant = Instant.ofEpochMilli(internalDate);
    DateTimeFormatter formatter =
        DateTimeFormatter.ISO_INSTANT;
    return formatter.format(instant);
  }

  private static String getHeaderValue(List<MessagePartHeader> headers, String headerName) {
    if (headers == null) {
      return null;
    }
    for (MessagePartHeader header : headers) {
      if (header.getName().equalsIgnoreCase(headerName)) {
        return header.getValue();
      }
    }
    return null;
  }

  private static String getBodyPreview(Message fullMessage) throws IOException {
    if (fullMessage.getSnippet() != null) {
      return fullMessage.getSnippet();
    }
    String body = getPlainTextContent(fullMessage);
    if (body != null && body.length() > 200) {
      return body.substring(0, 200) + "..."; // Truncate for preview
    }

    return body;
  }

  private static String getPlainTextContent(Message fullMessage) throws IOException {
    if (fullMessage.getPayload() == null) {
      return null;
    }
    String content = getPlainTextFromPart(fullMessage.getPayload());
    if (content == null && fullMessage.getPayload().getParts() != null) {
      for (MessagePart part : fullMessage.getPayload().getParts()) {
        content = getPlainTextFromPart(part);
        if (content != null) {
          break;
        }
      }
    }
    return content;
  }

  private static String getPlainTextFromPart(MessagePart part) throws IOException {
    if (part == null) {
      return null;
    }
    if ("text/plain".equalsIgnoreCase(part.getMimeType())) {
      return decodeBase64(part.getBody().getData());
    }
    if (part.getParts() != null) {
      for (MessagePart subPart : part.getParts()) {
        String content = getPlainTextFromPart(subPart);
        if (content != null) {
          return content;
        }
      }
    }
    return null;
  }

  private static String getHtmlContent(Message fullMessage) throws IOException {
    if (fullMessage.getPayload() == null) {
      return null;
    }
    String content = getHtmlFromPart(fullMessage.getPayload());
    if (content == null && fullMessage.getPayload().getParts() != null) {
      for (MessagePart part : fullMessage.getPayload().getParts()) {
        content = getHtmlFromPart(part);
        if (content != null) {
          break;
        }
      }
    }
    return content;
  }

  private static String getHtmlFromPart(MessagePart part) throws IOException {
    if (part == null) {
      return null;
    }
    if ("text/html".equalsIgnoreCase(part.getMimeType())) {
      return decodeBase64(part.getBody().getData());
    }
    if (part.getParts() != null) {
      for (MessagePart subPart : part.getParts()) {
        String content = getHtmlFromPart(subPart);
        if (content != null) {
          return content;
        }
      }
    }
    return null;
  }

  private static String decodeBase64(String data) throws IOException {
    if (data == null) {
      return null;
    }
    byte[] decodedBytes = Base64.getUrlDecoder().decode(data);
    return new String(decodedBytes, StandardCharsets.UTF_8);
  }
}

package com.bindord.financemanagement.model.mapper;

import com.bindord.financemanagement.model.record.ProviderMailMessage;
import com.bindord.financemanagement.model.source.MailMessage;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Component
public class MailMessageMapper {

  public MailMessage toEntity(ProviderMailMessage msg) {

    return MailMessage.builder()
        .id(msg.id())
        .createdDateTime(convertDatetimeToUTCMinusFive(msg.createdDateTime()))
        .subject(msg.subject())
        .bodyPreview(msg.bodyPreview())
        .bodyHtml(msg.bodyHtml())
        .bodyTextContent(extractTextContent(msg.bodyHtml()))
        .fromEmail(msg.from())
        .webLink(msg.webLink())
        .referenceId(msg.id())
        .build();
  }

  public List<MailMessage> toEntities(List<ProviderMailMessage> messages) {
    return messages.stream()
        .map(this::toEntity)
        .toList();
  }

  private LocalDateTime convertDatetimeToUTCMinusFive(String dateTime) {
    return OffsetDateTime
        .parse(dateTime)
        .withOffsetSameInstant(ZoneOffset.ofHours(-5))
        .toLocalDateTime();
  }

  /**
   * Basic HTML → text extraction.
   * Replace with Jsoup if you want a stronger solution.
   */
  private String extractTextContent(String html) {
    if (html == null) {
      return "";
    }
    return html.replaceAll("<[^>]*>", "");
  }
}
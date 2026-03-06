package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.model.record.ProviderMailMessage;
import com.bindord.financemanagement.model.source.OutlookMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OutlookMailProviderFacade implements MailProviderFacade {

  private final OutlookFacade outlookFacade;

  @Override
  public List<ProviderMailMessage> findMessagesSince(String token, LocalDateTime dateTime) {

    List<OutlookMessageDto> outlookMessages =
        outlookFacade.findByCreatedDateTimeGreaterThan(token, dateTime);

    return outlookMessages.stream()
        .map(this::toMailMessage)
        .toList();
  }

  private ProviderMailMessage toMailMessage(OutlookMessageDto dto) {

    String bodyHtml = dto.getBody() != null
        ? dto.getBody().getContent()
        : null;

    String from = null;

    if (dto.getFrom() != null) {
      if (dto.getFrom().getEmailAddress() != null) {
        from = dto.getFrom().getEmailAddress().getAddress();
      } else {
        from = dto.getFrom().getAddress();
      }
    }

    return new ProviderMailMessage(
        dto.getId(),
        dto.getSubject(),
        bodyHtml,
        dto.getBodyPreview(),
        dto.getCreatedDateTime(),
        from,
        dto.getWebLink()
    );
  }
}

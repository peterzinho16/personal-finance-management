package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.model.record.ProviderMailMessage;
import com.bindord.financemanagement.model.source.GmailMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GmailMailProviderFacade implements MailProviderFacade {

  private final GmailFacade gmailFacade;

  @Override
  public List<ProviderMailMessage> findMessagesSince(String token, LocalDateTime dateTime) throws GeneralSecurityException, IOException {
    log.info("Parameter token not required here");

    List<GmailMessageDto> gmailMessages =
        gmailFacade.findByCreatedDateTimeGreaterThan(dateTime);

    return gmailMessages.stream()
        .map(this::toMailMessage)
        .toList();
  }

  private ProviderMailMessage toMailMessage(GmailMessageDto dto) {
    return new ProviderMailMessage(
        dto.getId(),
        dto.getSubject(),
        dto.getBodyHTML(),
        dto.getBodyPreview(),
        dto.getCreatedDateTime(),
        dto.getFrom(),
        dto.getWebLink()
    );
  }
}
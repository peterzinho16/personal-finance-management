package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.model.source.MailMessagesResponse;
import com.bindord.financemanagement.model.source.MessageDto;
import com.bindord.financemanagement.repository.external.MicrosoftGraphClient;
import com.bindord.financemanagement.utils.Constants;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class EmailFacadeImpl implements EmailFacade {

  public static final Integer FIXED_TOP_RECORDS = 250;
  private final MicrosoftGraphClient microsoftGraphClient;

  /**
   * Find all the messages greater than the specified createdDateTime
   *
   * @param createdDateTime the date and time to compare against
   * @return a list of {@link MessageDto} objects
   */
  @Override
  public List<MessageDto> findByCreatedDateTimeGreaterThan(String token, LocalDateTime createdDateTime) {
    MailMessagesResponse mailMessagesResponse = microsoftGraphClient.findByCreatedDateTimeGreaterThan(
        Constants.INBOX_FOLDER_ID,
        Constants.NOTIF_COMPRAS_SUB_FOLDER_ID,
        FIXED_TOP_RECORDS,
        "createdDateTime gt " + createdDateTime.atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT),
        token
    );
    return mailMessagesResponse.getValue();
  }
}

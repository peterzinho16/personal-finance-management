package com.bindord.financemanagement.resolver;

import com.bindord.financemanagement.svc.GmailMailProviderFacade;
import com.bindord.financemanagement.svc.MailProviderFacade;
import com.bindord.financemanagement.svc.OutlookMailProviderFacade;
import com.bindord.financemanagement.utils.enums.MailProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailProviderResolver {

  private final GmailMailProviderFacade gmailProvider;
  private final OutlookMailProviderFacade outlookProvider;

  public MailProviderFacade resolve(MailProvider provider) {

    return switch (provider) {
      case GMAIL -> gmailProvider;
      case OUTLOOK -> outlookProvider;
    };
  }
}
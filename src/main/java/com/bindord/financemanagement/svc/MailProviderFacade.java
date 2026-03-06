package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.model.record.ProviderMailMessage;

import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.List;

public interface MailProviderFacade {

  List<ProviderMailMessage> findMessagesSince(String token, LocalDateTime dateTime) throws GeneralSecurityException, Exception;

}

package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.model.source.GmailMessageDto;
import com.google.api.services.gmail.Gmail;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.List;

public interface GmailFacade {

  List<GmailMessageDto> findByCreatedDateTimeGreaterThan(LocalDateTime createdDateTime) throws GeneralSecurityException, IOException;

  Gmail getGmailService() throws IOException, GeneralSecurityException;
}

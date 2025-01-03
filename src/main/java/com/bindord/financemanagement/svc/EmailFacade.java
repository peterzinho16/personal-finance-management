package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.model.source.MessageDto;

import java.time.LocalDateTime;
import java.util.List;

public interface EmailFacade {

  List<MessageDto> findByCreatedDateTimeGreaterThan(String token, LocalDateTime createdDateTime);
}

package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.model.source.OutlookMessageDto;

import java.time.LocalDateTime;
import java.util.List;

public interface OutlookFacade {

  List<OutlookMessageDto> findByCreatedDateTimeGreaterThan(String token, LocalDateTime createdDateTime);
}

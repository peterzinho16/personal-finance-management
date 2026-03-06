package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.utils.enums.MailProvider;

public interface ExpenditureSyncService {

  String executeSynchronization(String accessToken, MailProvider mailProvider) throws Exception;
}

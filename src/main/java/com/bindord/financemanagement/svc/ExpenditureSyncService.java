package com.bindord.financemanagement.svc;

public interface ExpenditureSyncService {

  void executeSynchronization(String accessToken) throws Exception;
}

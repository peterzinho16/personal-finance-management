package com.bindord.financemanagement.svc;

public interface PayeeCategorizationService {

  void managePayeeCategorization(String payee, Integer subCategoryId);

  Integer obtainSubCategoryByPayee(String payee);
}

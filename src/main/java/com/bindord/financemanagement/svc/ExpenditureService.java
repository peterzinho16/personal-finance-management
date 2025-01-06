package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.model.finance.Expenditure;

public interface ExpenditureService {

  Expenditure updateSubCategoryById(Integer subCategoryId, Integer id, String payee) throws Exception;
}

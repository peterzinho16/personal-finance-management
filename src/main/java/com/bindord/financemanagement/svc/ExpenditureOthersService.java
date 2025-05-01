package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.model.finance.ExpenditureOthers;
import com.bindord.financemanagement.model.finance.ExpenditureUpdateFormDto;

public interface ExpenditureOthersService {

  ExpenditureOthers findById(Integer id) throws Exception;

  ExpenditureOthers updateSharedById(Integer id) throws Exception;

  ExpenditureOthers updateById(ExpenditureUpdateFormDto expenditureUpdateFormDto, Integer id) throws Exception;

  ExpenditureOthers updateSubCategoryById(Integer subCategoryId, Integer id, String payee) throws Exception;

  ExpenditureOthers updateSubCategoryInTableExpenditureOnly(Integer subCategoryId, Integer id) throws Exception;

}

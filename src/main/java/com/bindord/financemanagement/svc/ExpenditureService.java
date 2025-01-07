package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.model.finance.Expenditure;
import com.bindord.financemanagement.model.finance.ExpenditureUpdateDto;

public interface ExpenditureService {

  Expenditure findById(Integer id) throws Exception;

  Expenditure updateById(ExpenditureUpdateDto expenditureDto, Integer id) throws Exception;

  Expenditure updateSubCategoryById(Integer subCategoryId, Integer id, String payee) throws Exception;

  Expenditure updateSharedById(Integer id) throws Exception;
}

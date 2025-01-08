package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.advice.CustomValidationException;
import com.bindord.financemanagement.model.finance.Expenditure;
import com.bindord.financemanagement.model.finance.ExpenditureAddDto;
import com.bindord.financemanagement.model.finance.ExpenditureUpdateDto;

import java.security.NoSuchAlgorithmException;

public interface ExpenditureService {

  Expenditure findById(Integer id) throws Exception;

  Expenditure updateById(ExpenditureUpdateDto expenditureDto, Integer id) throws Exception;

  Expenditure updateSubCategoryById(Integer subCategoryId, Integer id, String payee) throws Exception;

  Expenditure updateSharedById(Integer id) throws Exception;

  Expenditure saveNewManually(ExpenditureAddDto expenditureDto) throws CustomValidationException, NoSuchAlgorithmException;
}

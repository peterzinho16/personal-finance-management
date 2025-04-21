package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.model.finance.ExpenditureOthers;

public interface ExpenditureOthersService {

  ExpenditureOthers findById(Integer id) throws Exception;

  void deleteById(Integer id) throws Exception;
}

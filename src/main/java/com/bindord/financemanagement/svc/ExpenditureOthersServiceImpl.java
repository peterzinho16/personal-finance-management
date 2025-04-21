package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.model.finance.ExpenditureOthers;
import com.bindord.financemanagement.repository.ExpenditureOthersRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class ExpenditureOthersServiceImpl implements ExpenditureOthersService {

  private final ExpenditureOthersRepository repository;

  /**
   * @param id integer
   * @return ExpenditureOthers
   */
  @Override
  public ExpenditureOthers findById(Integer id) throws Exception {
    return repository.findById(id).orElseThrow(() -> new Exception("Id " +
        "doesn't" +
        " exists"));
  }

  /**
   * @param id integer
   */
  @Override
  public void deleteById(Integer id) {
    repository.deleteById(id);
  }

}

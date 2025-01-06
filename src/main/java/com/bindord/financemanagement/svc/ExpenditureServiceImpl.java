package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.model.finance.Expenditure;
import com.bindord.financemanagement.repository.ExpenditureRepository;
import com.bindord.financemanagement.repository.PayeeCategorizationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@AllArgsConstructor
public class ExpenditureServiceImpl implements ExpenditureService {

  private final ExpenditureRepository expenditureRepository;
  private final PayeeCategorizationRepository payeeCategorizationRepository;

  /**
   * update subCategoryID on entity {@link Expenditure} and
   * {@link com.bindord.financemanagement.model.finance.PayeeCategorization}
   *
   * @param subCategoryId
   * @param id
   * @param payee
   * @return
   */
  @Override
  public Expenditure updateSubCategoryById(Integer subCategoryId, Integer id, String payee) throws Exception {
    if (payee != null) {
      var lwrPayee = payee.toLowerCase();
      var payeeId =
          payeeCategorizationRepository.findIdByLowerPayee(lwrPayee);
      if (payeeId != null) {
        payeeCategorizationRepository.updateSubCategoryByPayeeId(subCategoryId, payeeId);
      }
    }
    expenditureRepository.updateSubCategoryByPayeeId(subCategoryId, id);
    return expenditureRepository.findById(id).orElseThrow(() -> new Exception("Id doesn't" +
        " exists"));
  }
}

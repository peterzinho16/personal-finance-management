package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.repository.PayeeCategorizationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@AllArgsConstructor
public class PayeeCategorizationServiceImpl implements PayeeCategorizationService {

  private final PayeeCategorizationRepository payeeCategorizationRepository;

  /**
   * Manage the state of payee categorization
   * doing insert if necessary or just updating the totalEvents field
   *
   * @param payee         payee name
   * @param subCategoryId subCategoryId
   */
  @Override
  public void managePayeeCategorization(String payee, Integer subCategoryId) {
    if (payee != null) {
      var lwrPayee = payee.toLowerCase();
      var payeeExists =
          payeeCategorizationRepository.existsByLowerPayee(lwrPayee);
      if (payeeExists == null) {
        payeeCategorizationRepository.insertPayeeCategorizationAndDoNothingOnConflict(payee,
            LocalDateTime.now(),
            subCategoryId);
      } else {
        payeeCategorizationRepository.updateEventsByLowerPayee(lwrPayee, LocalDateTime.now());
      }
    }
  }

  /**
   * Retrieve the subCategoryId queried by lowerPayee
   *
   * @param payee is the payee name
   * @return Integer
   */
  @Override
  public Integer obtainSubCategoryByPayee(String payee) {
    return payeeCategorizationRepository.findSubCategoryIdByLowerPayee(payee.toLowerCase());
  }
}

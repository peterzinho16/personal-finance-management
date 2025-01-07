package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.advice.CustomValidationException;
import com.bindord.financemanagement.model.finance.Expenditure;
import com.bindord.financemanagement.model.finance.ExpenditureUpdateDto;
import com.bindord.financemanagement.repository.ExpenditureRepository;
import com.bindord.financemanagement.repository.PayeeCategorizationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static java.util.Objects.nonNull;

@Slf4j
@Service
@AllArgsConstructor
public class ExpenditureServiceImpl implements ExpenditureService {

  private final ExpenditureRepository repository;
  private final PayeeCategorizationRepository payeeCategorizationRepository;

  /**
   * @param id
   * @return
   */
  @Override
  public Expenditure findById(Integer id) throws Exception {
    return repository.findById(id).orElseThrow(() -> new Exception("Id " +
        "doesn't" +
        " exists"));
  }

  /**
   * @param expenditureDto
   * @param id
   * @return
   */
  @Override
  public Expenditure updateById(ExpenditureUpdateDto expenditureDto, Integer id) throws Exception {
    var qExpenditure = this.findById(id);
    if (nonNull(expenditureDto.getShared()) && qExpenditure.getShared() != expenditureDto.getShared()) {
      updateSharedState(qExpenditure);
    }
    if (nonNull(expenditureDto.getLent()) && qExpenditure.getLent() != expenditureDto.getLent()) {
      updateLentState(qExpenditure, expenditureDto);
    }

    if (qExpenditure.getShared() && qExpenditure.getLent()) {
      var msg = "The expenditure can be updated because both shared and lent can't be true at the same" +
          " " +
          "time";
      log.warn(msg);
      throw new CustomValidationException(msg);
    }

    var subCatId = expenditureDto.getSubCategoryId();
    var qSubCatId = qExpenditure.getSubCategory().getId();
    var expenditureResponse = repository.save(qExpenditure);
    if (expenditureDto.getSubCategoryId() != null && !subCatId.equals(qSubCatId)) {
      expenditureResponse = updateSubCategoryById(expenditureDto.getSubCategoryId(), id, qExpenditure.getPayee());
    }
    return expenditureResponse;
  }

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
    repository.updateSubCategoryByPayeeId(subCategoryId, id);
    return repository.findById(id).orElseThrow(() -> new Exception("Id doesn't" +
        " exists"));
  }

  /**
   * @param id
   * @return
   * @throws Exception
   */
  @Override
  public Expenditure updateSharedById(Integer id) throws Exception {
    var expenditure = this.findById(id);
    updateSharedState(expenditure);

    if (expenditure.getShared() && expenditure.getLent()) {
      var msg = "The expenditure can be updated because shared and lent can't be true at the same" +
          " " +
          "time";
      log.warn(msg);
      throw new CustomValidationException(msg);
    }
    return repository.save(expenditure);
  }

  private static void updateSharedState(Expenditure expenditure) {
    var sharedState = !expenditure.getShared();
    expenditure.setShared(sharedState);
    expenditure.setSharedAmount(sharedState ? expenditure.getAmount() / 2 : null);
  }

  private static void updateLentState(Expenditure expenditure,
                                      ExpenditureUpdateDto expenditureUpdateDto) {
    var nwLentValue = expenditureUpdateDto.getLent();
    expenditure.setLent(nwLentValue);
    expenditure.setLentTo(nwLentValue ? expenditureUpdateDto.getLentTo() : null);
    expenditure.setLoanAmount(nwLentValue ? expenditure.getAmount() : null);
    expenditure.setLoanState(nwLentValue ? Expenditure.LoanState.PENDING : null);
  }
}

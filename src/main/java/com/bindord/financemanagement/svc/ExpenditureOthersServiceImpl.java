package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.advice.CustomValidationException;
import com.bindord.financemanagement.model.finance.Expenditure;
import com.bindord.financemanagement.model.finance.ExpenditureOthers;
import com.bindord.financemanagement.model.finance.ExpenditureUpdateDto;
import com.bindord.financemanagement.model.finance.ExpenditureUpdateFormDto;
import com.bindord.financemanagement.repository.ExpenditureOthersRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.bindord.financemanagement.utils.Constants.MSG_ERROR_SHARED_ALREADY;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@AllArgsConstructor
public class ExpenditureOthersServiceImpl implements ExpenditureOthersService {

  private final ExpenditureOthersRepository repository;
  private final ExpenditureServiceImpl expenditureOriginalService;

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
   * update subCategoryID on entity {@link Expenditure} and
   * {@link com.bindord.financemanagement.model.finance.PayeeCategorization}
   *
   * @param subCategoryId
   * @param id
   * @param payee
   * @return
   */
  @Override
  public ExpenditureOthers updateSubCategoryById(Integer subCategoryId, Integer id, String payee) throws Exception {
    expenditureOriginalService.updatePayeeCategorization(subCategoryId, payee);
    repository.updateSubCategoryById(subCategoryId, id);
    return repository.findById(id).orElseThrow(() -> new Exception("Id doesn't" +
        " exists"));
  }

  /**
   * @param id
   * @return
   * @throws Exception
   */
  @Transactional
  @Override
  public ExpenditureOthers updateSharedById(Integer id) throws Exception {
    var expenditureOther = this.findById(id);

    if (expenditureOther.getShared() != null && expenditureOther.getShared()) {
      var msg = MSG_ERROR_SHARED_ALREADY;
      log.warn(msg);
      throw new CustomValidationException(msg);
    }
    updateSharedState(expenditureOther);
    return repository.save(expenditureOther);
  }

  /**
   * @param expenditureUpdateFormDto
   * @param id
   * @return
   * @throws Exception
   */
  @Override
  public ExpenditureOthers updateById(ExpenditureUpdateFormDto expenditureUpdateFormDto,
                                      Integer id) throws Exception {
    ExpenditureUpdateDto expenditureDto = expenditureUpdateFormDto.getExpenditureUpdateDto();
    var qExpenditure = this.findById(id);

    if (nonNull(expenditureDto.getDescription())
        && !expenditureDto.getDescription().isEmpty()
        && !qExpenditure.getDescription().equals(expenditureDto.getDescription())) {
      qExpenditure.setDescription(expenditureDto.getDescription());
    }

    if (nonNull(expenditureDto.getShared()) && qExpenditure.getShared() != expenditureDto.getShared()) {
      updateSharedState(qExpenditure);
    }

    if (nonNull(expenditureDto.getForDaughter())
        && qExpenditure.getForDaughter() != expenditureDto.getForDaughter()) {
      qExpenditure.setForDaughter(expenditureDto.getForDaughter());
    }

    var subCatId = expenditureDto.getSubCategoryId();
    var qSubCatId = qExpenditure.getSubCategory().getId();
    var expenditureResponse = repository.save(qExpenditure);
    if (expenditureDto.getSubCategoryId() != null && !subCatId.equals(qSubCatId)) {
      if (expenditureUpdateFormDto.getFormBehaviour().getUpdateWithoutPayeeCategorization()) {
        expenditureResponse =
            updateSubCategoryInTableExpenditureOnly(expenditureDto.getSubCategoryId(), id);
      } else {
        expenditureResponse = updateSubCategoryById(expenditureDto.getSubCategoryId(), id,
            qExpenditure.getPayee());
      }
    }
    return expenditureResponse;
  }

  private static void updateSharedState(ExpenditureOthers expenditure) {
    var sharedState = !expenditure.getShared();
    expenditure.setShared(sharedState);
    expenditure.setSharedAmount(sharedState ? expenditure.getAmount() / 2 : null);
  }

  /**
   * update subCategoryID only on entity {@link Expenditure}
   *
   * @param subCategoryId
   * @param id
   * @return
   */
  public ExpenditureOthers updateSubCategoryInTableExpenditureOnly(Integer subCategoryId, Integer id) throws Exception {
    repository.updateSubCategoryById(subCategoryId, id);
    return repository.findById(id).orElseThrow(() -> new Exception("Id doesn't" +
        " exists"));
  }


}

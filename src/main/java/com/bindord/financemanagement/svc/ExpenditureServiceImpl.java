package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.advice.CustomValidationException;
import com.bindord.financemanagement.config.AppDataConfiguration;
import com.bindord.financemanagement.model.finance.Expenditure;
import com.bindord.financemanagement.model.finance.ExpenditureAddDto;
import com.bindord.financemanagement.model.finance.ExpenditureInstallment;
import com.bindord.financemanagement.model.finance.ExpenditureUpdateDto;
import com.bindord.financemanagement.model.finance.ExpenditureUpdateFormDto;
import com.bindord.financemanagement.model.finance.RecurrentExpenditure;
import com.bindord.financemanagement.repository.ExpenditureInstallmentRepository;
import com.bindord.financemanagement.repository.ExpenditureRepository;
import com.bindord.financemanagement.repository.PayeeCategorizationRepository;
import com.bindord.financemanagement.repository.SubCategoryRepository;
import com.bindord.financemanagement.utils.Utilities;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import static com.bindord.financemanagement.model.finance.Expenditure.Currency.PEN;
import static com.bindord.financemanagement.model.finance.Expenditure.Currency.USD;
import static com.bindord.financemanagement.utils.Constants.MSG_ERROR_INSTALLMENTS_NOT_MODIFICATION_ALLOWED;
import static com.bindord.financemanagement.utils.Constants.MSG_ERROR_SHARED_AND_LENT_AND_BORROWED;
import static com.bindord.financemanagement.utils.Utilities.convertNumberToOnlyTwoDecimals;
import static com.bindord.financemanagement.utils.Utilities.getLocalDateTimeNowWithFormat;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@AllArgsConstructor
public class ExpenditureServiceImpl implements ExpenditureService {

  private final ExpenditureRepository repository;
  private final PayeeCategorizationRepository payeeCategorizationRepository;
  private final SubCategoryRepository subCategoryRepository;
  private final ExpenditureInstallmentRepository expenditureInstallmentRepository;
  private final AppDataConfiguration appDataConfiguration;

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
   * @param expenditureUpdateFormDto
   * @param id
   * @return {@link Expenditure}
   */
  @Override
  public Expenditure updateById(ExpenditureUpdateFormDto expenditureUpdateFormDto, Integer id) throws Exception {
    ExpenditureUpdateDto expenditureDto = expenditureUpdateFormDto.getExpenditureUpdateDto();
    var qExpenditure = this.findById(id);
    var newInstallmentsValue = expenditureDto.getInstallments();
    var installmentsWasModified = !qExpenditure.getInstallments().equals(newInstallmentsValue);
    if (qExpenditure.getInstallments() > 1
        && installmentsWasModified) {
      var msg = MSG_ERROR_INSTALLMENTS_NOT_MODIFICATION_ALLOWED;
      log.warn(msg);
      throw new CustomValidationException(msg);
    }

    if (nonNull(expenditureDto.getDescription())
        && !expenditureDto.getDescription().isEmpty()
        && !qExpenditure.getDescription().equals(expenditureDto.getDescription())) {
      qExpenditure.setDescription(expenditureDto.getDescription());
    }

    if (nonNull(newInstallmentsValue) && newInstallmentsValue > 0 && installmentsWasModified) {
      updateExpenditureWithInstallments(qExpenditure, newInstallmentsValue);
    }

    if (nonNull(expenditureDto.getShared()) && qExpenditure.getShared() != expenditureDto.getShared()) {
      updateSharedState(qExpenditure);
    }
    if (nonNull(expenditureDto.getLent()) && qExpenditure.getLent() != expenditureDto.getLent()) {
      updateLentState(qExpenditure, expenditureDto);
    }

    if (nonNull(expenditureDto.getForDaughter())
        && qExpenditure.getForDaughter() != expenditureDto.getForDaughter()) {
      qExpenditure.setForDaughter(expenditureDto.getForDaughter());
    }

    if (qExpenditure.getShared() && qExpenditure.getLent()) {
      var msg = MSG_ERROR_SHARED_AND_LENT_AND_BORROWED;
      log.warn(msg);
      throw new CustomValidationException(msg);
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

  private void updateExpenditureWithInstallments(Expenditure qExpenditure,
                                                 Short newInstallmentsValue) {
    var installmentAmount =
        convertNumberToOnlyTwoDecimals(qExpenditure.getAmount() / newInstallmentsValue);
    var expInstObj = expenditureInstallmentMapper(qExpenditure, installmentAmount,
        newInstallmentsValue);
    ExpenditureInstallment expInstPersisted =
        expenditureInstallmentRepository.save(expInstObj);
    qExpenditure.setExpenditureInstallmentId(expInstPersisted.getId());
    qExpenditure.setInstallments(newInstallmentsValue);
    qExpenditure.setAmount(installmentAmount);
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
    updatePayeeCategorization(subCategoryId, payee);
    repository.updateSubCategoryById(subCategoryId, id);
    return repository.findById(id).orElseThrow(() -> new Exception("Id doesn't" +
        " exists"));
  }

  //⚠️This method is utilized in both this class and the ExpenditureOtherServiceImpl class.
  public void updatePayeeCategorization(Integer subCategoryId, String payee) {
    if (payee != null) {
      var lwrPayee = payee.toLowerCase();
      var payeeId =
          payeeCategorizationRepository.findIdByLowerPayee(lwrPayee);
      if (payeeId != null) {
        payeeCategorizationRepository.updateSubCategoryByPayeeId(subCategoryId, payeeId);
      }
    }
  }

  /**
   * update subCategoryID only on entity {@link Expenditure}
   *
   * @param subCategoryId
   * @param id
   * @return
   */
  public Expenditure updateSubCategoryInTableExpenditureOnly(Integer subCategoryId, Integer id) throws Exception {
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
  public Expenditure updateSharedById(Integer id) throws Exception {
    var expenditure = this.findById(id);
    updateSharedState(expenditure);
    var sharedVal = expenditure.getShared() != null ? expenditure.getShared() : false;
    var lentVal = expenditure.getLent() != null ? expenditure.getLent() : false;
    var wasBorrowVal = expenditure.getWasBorrowed() != null
        ? expenditure.getWasBorrowed() : false;

    if (sharedVal && lentVal || lentVal && wasBorrowVal || wasBorrowVal && sharedVal) {
      var msg = MSG_ERROR_SHARED_AND_LENT_AND_BORROWED;
      log.warn(msg);
      throw new CustomValidationException(msg);
    }
    return repository.save(expenditure);
  }

  /**
   * @param id
   * @throws Exception
   */
  @Override
  public void deleteById(Integer id) {
    repository.deleteById(id);
  }

  /**
   * @param expenditureDto obj with the properties of expenditure to be persisted
   * @return Expenditure obj that was persisted
   */
  @Transactional
  @Override
  public Expenditure saveNewManually(ExpenditureAddDto expenditureDto) throws CustomValidationException, NoSuchAlgorithmException {
    Expenditure expenditure = expenditureMapperForInsertOrImportManually(expenditureDto);
    Short totalInstallments = expenditureDto.getInstallments();
    if (Objects.nonNull(totalInstallments) && totalInstallments > 1) {
      updateExpenditureWithInstallments(expenditure, totalInstallments);
    }
    return repository.save(expenditure);
  }

  private ExpenditureInstallment expenditureInstallmentMapper(Expenditure expenditure,
                                                              double installmentAmount,
                                                              Short totalInstallments) {
    return ExpenditureInstallment.builder()
        .description(expenditure.getDescription())
        .payee(expenditure.getPayee())
        .subCategory(expenditure.getSubCategory())
        .amount(expenditure.getAmount())
        .installmentAmount(installmentAmount)
        .installments(totalInstallments)
        .transactionDate(expenditure.getTransactionDate())
        .finishDebtDate(expenditure.getTransactionDate().plusMonths(totalInstallments))
        .pendingAmount(convertNumberToOnlyTwoDecimals(expenditure.getAmount() - expenditure.getAmount() / totalInstallments))
        .currency(expenditure.getCurrency())
        .referenceId(expenditure.getReferenceId())
        .fullPaid(false)
        .build();
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

  private Expenditure expenditureMapperForInsertOrImportManually(ExpenditureAddDto expenditureDto) throws CustomValidationException, NoSuchAlgorithmException {
    var sharedVal = expenditureDto.getShared() != null ? expenditureDto.getShared() : false;
    var lentVal = expenditureDto.getLent() != null ? expenditureDto.getLent() : false;
    var wasBorrowVal = expenditureDto.getWasBorrowed() != null
        ? expenditureDto.getWasBorrowed() : false;

    if (sharedVal && lentVal || lentVal && wasBorrowVal || wasBorrowVal && sharedVal) {
      throw new CustomValidationException(MSG_ERROR_SHARED_AND_LENT_AND_BORROWED);
    }

    var payee = expenditureDto.getPayee();
    Double amount = expenditureDto.getAmount();
    var referenceId =
        Utilities.generateSha256FromMailIdOrPayee(expenditureDto.getTransactionDate(), payee);


    Double conversionToPen = null;
    if(USD.name().equals(expenditureDto.getCurrency())) {
      var usdExchangeRate = appDataConfiguration.getExchangeRateData().get(AppDataConfiguration.CURRENT_USD_EXCHANGE_RATE).getUsdExchangeRate();
      conversionToPen = usdExchangeRate.doubleValue() * amount;
    }

    return Expenditure.builder()
        .referenceId(Objects.nonNull(expenditureDto.getReferenceId()) && !expenditureDto.getReferenceId().isBlank() ?
            expenditureDto.getReferenceId() : referenceId)
        .description(expenditureDto.getDescription())
        .transactionDate(
            expenditureDto.getTransactionDate()
        )
        .payee(payee)
        .currency(PEN.name().equals(expenditureDto.getCurrency()) ? PEN : USD)
        .amount(
            amount
        )
        .shared(sharedVal)
        .sharedAmount(sharedVal ? amount / 2 : null)
        .conversionToPen(convertNumberToOnlyTwoDecimals(conversionToPen))
        .singlePayment(true)
        .installments(expenditureDto.getInstallments())
        .expenditureInstallmentId(null)
        .lent(lentVal)
        .lentTo(lentVal ? expenditureDto.getLentTo() : null)
        .loanState(lentVal ? Expenditure.LoanState.PENDING : null)
        .loanAmount(lentVal ? amount : null)
        .wasBorrowed(wasBorrowVal)
        .borrowedFrom(wasBorrowVal ? expenditureDto.getBorrowedFrom() : null)
        .borrowedState(wasBorrowVal ? Expenditure.LoanState.PENDING : null)
        .recurrent(false)
        .manualRegister(true)
        .expImported(expenditureDto.getExpImported())
        .forDaughter(expenditureDto.getForDaughter())
        .subCategory(subCategoryRepository
            .findById(expenditureDto.getSubCategoryId())
            .orElseThrow(() ->
                new CustomValidationException("Sub category doesn't exists!"))
        ).build();
  }

  public Expenditure expenditureMapperFromRecurrentExpenditure(
      RecurrentExpenditure recurrentExpenditure) throws NoSuchAlgorithmException,
      CustomValidationException {
    var expenditure = expenditureMapperForInsertOrImportManually(
        ExpenditureAddDto.builder()
            .description(recurrentExpenditure.getDescription())
            .payee(recurrentExpenditure.getDescription())
            .subCategoryId(recurrentExpenditure.getSubCategory().getId())
            .shared(false)
            .lent(false)
            .wasBorrowed(false)
            .amount(recurrentExpenditure.getAmount())
            .currency(PEN.name())
            .transactionDate(getLocalDateTimeNowWithFormat())
            .expImported(false)
            .installments((short) 1)
            .build());
    expenditure.setRecurrent(true);
    return expenditure;
  }

}

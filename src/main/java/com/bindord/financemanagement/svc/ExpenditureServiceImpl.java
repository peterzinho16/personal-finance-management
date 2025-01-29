package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.advice.CustomValidationException;
import com.bindord.financemanagement.model.finance.Expenditure;
import com.bindord.financemanagement.model.finance.ExpenditureAddDto;
import com.bindord.financemanagement.model.finance.ExpenditureInstallment;
import com.bindord.financemanagement.model.finance.ExpenditureUpdateDto;
import com.bindord.financemanagement.model.finance.RecurrentExpenditure;
import com.bindord.financemanagement.repository.ExpenditureInstallmentRepository;
import com.bindord.financemanagement.repository.ExpenditureRepository;
import com.bindord.financemanagement.repository.PayeeCategorizationRepository;
import com.bindord.financemanagement.repository.SubCategoryRepository;
import com.bindord.financemanagement.utils.Utilities;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

import static com.bindord.financemanagement.model.finance.Expenditure.Currency.PEN;
import static com.bindord.financemanagement.model.finance.Expenditure.Currency.USD;
import static com.bindord.financemanagement.utils.Constants.MSG_ERROR_INSTALLMENTS_NOT_MODIFICATION_ALLOWED;
import static com.bindord.financemanagement.utils.Constants.MSG_ERROR_SHARED_AND_LENT_AND_BORROWED;
import static com.bindord.financemanagement.utils.Utilities.convertNumberToOnlyTwoDecimals;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@AllArgsConstructor
public class ExpenditureServiceImpl implements ExpenditureService {

  private final ExpenditureRepository repository;
  private final PayeeCategorizationRepository payeeCategorizationRepository;
  private final SubCategoryRepository subCategoryRepository;
  private final ExpenditureInstallmentRepository expenditureInstallmentRepository;

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
    var newInstallmentsValue = expenditureDto.getInstallments();
    var installmentsWasModified = !qExpenditure.getInstallments().equals(newInstallmentsValue);
    if (qExpenditure.getInstallments() > 1
        && installmentsWasModified) {
      var msg = MSG_ERROR_INSTALLMENTS_NOT_MODIFICATION_ALLOWED;
      log.warn(msg);
      throw new CustomValidationException(msg);
    }

    if (nonNull(newInstallmentsValue) && newInstallmentsValue > 0 && installmentsWasModified) {
      var installmentAmount = qExpenditure.getAmount() / newInstallmentsValue;
      var expInstObj = expenditureInstallmentMapper(qExpenditure, installmentAmount,
          newInstallmentsValue);
      ExpenditureInstallment expInstPersisted =
          expenditureInstallmentRepository.save(expInstObj);
      qExpenditure.setExpenditureInstallmentId(expInstPersisted.getId());
      qExpenditure.setInstallments(newInstallmentsValue);
      qExpenditure.setAmount(installmentAmount);
    }

    if (nonNull(expenditureDto.getShared()) && qExpenditure.getShared() != expenditureDto.getShared()) {
      updateSharedState(qExpenditure);
    }
    if (nonNull(expenditureDto.getLent()) && qExpenditure.getLent() != expenditureDto.getLent()) {
      updateLentState(qExpenditure, expenditureDto);
    }
    if (nonNull(expenditureDto.getDescription())
        && !expenditureDto.getDescription().isEmpty()
        && !qExpenditure.getDescription().equals(expenditureDto.getDescription())) {
      qExpenditure.setDescription(expenditureDto.getDescription());
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
      expenditureResponse = updateSubCategoryById(expenditureDto.getSubCategoryId(), id,
          qExpenditure.getPayee());
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
   * @param expenditureDto obj with the properties of expenditure to be persisted
   * @return Expenditure obj that was persisted
   */
  @Override
  public Expenditure saveNewManually(ExpenditureAddDto expenditureDto) throws CustomValidationException, NoSuchAlgorithmException {
    Expenditure expenditure = expenditureMapperForManualInsert(expenditureDto);
    Short totalInstallments = expenditureDto.getInstallments();
    if (totalInstallments > 1) {
      var installmentAmount = expenditure.getAmount() / totalInstallments;
      var expInstallEntity = expenditureInstallmentMapper(expenditure, installmentAmount,
          totalInstallments);
      ExpenditureInstallment expInstPersisted =
          expenditureInstallmentRepository.save(expInstallEntity);
      expenditure.setExpenditureInstallmentId(expInstPersisted.getId());
      expenditure.setInstallments(totalInstallments);
      expenditure.setAmount(installmentAmount);
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

  private Expenditure expenditureMapperForManualInsert(ExpenditureAddDto expenditureDto) throws CustomValidationException, NoSuchAlgorithmException {
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
    return Expenditure.builder()
        .referenceId(referenceId)
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
    var expenditure = expenditureMapperForManualInsert(
        ExpenditureAddDto.builder()
            .description(recurrentExpenditure.getDescription())
            .payee(recurrentExpenditure.getDescription())
            .subCategoryId(recurrentExpenditure.getSubCategory().getId())
            .shared(false)
            .lent(false)
            .wasBorrowed(false)
            .amount(recurrentExpenditure.getAmount())
            .currency(PEN.name())
            .transactionDate(LocalDateTime.now())
            .build());
    expenditure.setRecurrent(true);
    return expenditure;
  }

}

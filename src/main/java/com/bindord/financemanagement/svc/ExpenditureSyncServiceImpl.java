package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.config.AppDataConfiguration;
import com.bindord.financemanagement.model.finance.Category;
import com.bindord.financemanagement.model.finance.Expenditure;
import com.bindord.financemanagement.model.finance.ExpenditureInstallment;
import com.bindord.financemanagement.model.finance.RecurrentExpenditure;
import com.bindord.financemanagement.model.finance.SubCategory;
import com.bindord.financemanagement.model.source.MailExclusionRule;
import com.bindord.financemanagement.model.source.MailMessage;
import com.bindord.financemanagement.model.source.MessageDto;
import com.bindord.financemanagement.model.source.Parameter;
import com.bindord.financemanagement.repository.CategoryRepository;
import com.bindord.financemanagement.repository.ExpenditureInstallmentRepository;
import com.bindord.financemanagement.repository.ExpenditureRepository;
import com.bindord.financemanagement.repository.MailExclusionRuleRepository;
import com.bindord.financemanagement.repository.MailMessageRepository;
import com.bindord.financemanagement.repository.ParameterRepository;
import com.bindord.financemanagement.repository.PayeeCoincidenceRepository;
import com.bindord.financemanagement.repository.RecurrentExpenditureRepository;
import com.bindord.financemanagement.repository.SubCategoryRepository;
import com.bindord.financemanagement.utils.ExpenditureExtractorUtil;
import com.bindord.financemanagement.utils.MailRegex;
import com.bindord.financemanagement.utils.Utilities;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.bindord.financemanagement.utils.Constants.DEFAULT_EXPENDITURE_CATEGORY;
import static com.bindord.financemanagement.utils.MailRegex.extractExpenditureAmount;
import static com.bindord.financemanagement.utils.Utilities.convertDatetimeToUTCMinusFive;
import static com.bindord.financemanagement.utils.Utilities.convertNumberToOnlyTwoDecimals;

@Slf4j
@AllArgsConstructor
@Service
public class ExpenditureSyncServiceImpl implements ExpenditureSyncService {

  private static final String LAST_SYNC_DATE = "LAST_SYNC_DATE";
  private static final String LAST_RECURRENTS_SYNC_DATE = "LAST_RECURRENT_SYNC_DATE";


  private final MailExclusionRuleRepository mailExclusionRuleRepository;
  private final EmailFacade emailFacade;
  private final ParameterRepository parameterRepository;
  private final ExpenditureRepository expenditureRepository;
  private final CategoryRepository categoryRepository;
  private final SubCategoryRepository subCategoryRepository;
  private final MailMessageRepository mailMessageRepository;
  private final PayeeCategorizationService payeeCategorizationService;
  private final PayeeCoincidenceRepository payeeCoincidenceRepository;
  private final RecurrentExpenditureRepository recurrentExpenditureRepository;
  private final ExpenditureServiceImpl expenditureService;
  private final ExpenditureInstallmentRepository expenditureInstallmentRepository;
  private final AppDataConfiguration appDataConfiguration;

  /**
   * Execute synchronization from outlook to expenditure table
   *
   * @param accessToken token associated to Microsoft Graph API
   */
  @Override
  public String executeSynchronization(String accessToken) throws Exception {

    List<MailExclusionRule> exclusionsList = mailExclusionRuleRepository.findAll();
    Set<String> exclusions =
        exclusionsList.stream().map(MailExclusionRule::getKeyword).collect(Collectors.toSet());
    Parameter parameter =
        parameterRepository.findById(LAST_SYNC_DATE).orElseThrow(() -> new Exception("Error " +
            "while" + " trying to get the last sync date from parameters table"));
    String paramLastSyncDateTime = parameter.getValue();
    OffsetDateTime lastSyncDateTime = OffsetDateTime.parse(paramLastSyncDateTime);
    List<MessageDto> beforeFilterMessages =
        emailFacade.findByCreatedDateTimeGreaterThan(accessToken,
            lastSyncDateTime.toLocalDateTime().plusMinutes(300).plusSeconds(1));

    if (beforeFilterMessages.isEmpty()) {
      var msg = "There is no record to register";
      log.info(msg);
      return msg;
    }

    List<MessageDto> postFilterMessages = Utilities.getFilteredMessages(beforeFilterMessages,
        exclusions);


    //Preparation
    Category category = categoryRepository.findByName(DEFAULT_EXPENDITURE_CATEGORY);
    SubCategory subCategory = subCategoryRepository.findByCategoryIdAndName(category.getId(),
        DEFAULT_EXPENDITURE_CATEGORY);
    subCategory.setCategory(category);
    List<Expenditure> expenditures = new ArrayList<>();
    List<MailMessage> mailMessages = new ArrayList<>();

    // Parse the string to OffsetDateTime
    OffsetDateTime lastRecordDateTime =
        OffsetDateTime.parse(postFilterMessages.getLast().getCreatedDateTime());
    // Subtract 5 hours
    OffsetDateTime updatedLastRecordDateTime = lastRecordDateTime.minusHours(5);
    String lastMessageDateTime = updatedLastRecordDateTime.toString();

    for (MessageDto msg : postFilterMessages) {
      String payee = buildEntitiesAndGetPayee(msg, subCategory, expenditures, mailMessages);

      payeeCategorizationService.managePayeeCategorization(payee, subCategory.getId());
    }
    expenditureRepository.saveAll(expenditures);
    mailMessageRepository.saveAll(mailMessages);

    Parameter paramRecurrents =
        parameterRepository.findById(LAST_RECURRENTS_SYNC_DATE).orElseThrow(() -> new Exception(
            "Error while trying to get the last sync date from parameters table"));
    if (paramRecurrents.getValue() == null || updatedLastRecordDateTime.getMonth() != OffsetDateTime.parse(paramRecurrents.getValue()).getMonth()) {
      paramRecurrents.setValue(lastMessageDateTime);
      parameterRepository.save(paramRecurrents);
      List<RecurrentExpenditure> recurrents =
          recurrentExpenditureRepository.findAllEnabledWithSubCategoryAndCat();
      List<Expenditure> recurrentExpenditureList = new ArrayList<>();
      for (RecurrentExpenditure reccExpend : recurrents) {
        recurrentExpenditureList.add(expenditureService.expenditureMapperFromRecurrentExpenditure(reccExpend));
      }
      expenditureRepository.saveAll(recurrentExpenditureList);
    }
    parameter.setValue(lastMessageDateTime);
    parameterRepository.save(parameter);

    List<ExpenditureInstallment> expenditureInstallments =
        expenditureInstallmentRepository.findAllByFullPaidIsFalse();

    var now = LocalDateTime.now(ZoneOffset.UTC).minusHours(5);
    for (ExpenditureInstallment expenditureInstallment : expenditureInstallments) {
      List<Expenditure> expenditureList =
          expenditureRepository.findAllByExpenditureInstallmentIdOrderById(expenditureInstallment.getId());

      var initDate = expenditureInstallment.getTransactionDate();
      var projectionDate = initDate.plusMonths(expenditureList.size());
      if (now.isAfter(projectionDate)) {
        var expenditureReference = expenditureList.getFirst();
        expenditureReference.setId(null);
        var newExpenditure = new Expenditure();
        BeanUtils.copyProperties(expenditureReference, newExpenditure);
        newExpenditure.setTransactionDate(projectionDate);
        newExpenditure.setDescription(expenditureInstallment.getDescription());
        expenditureRepository.save(newExpenditure);
        var newPendingAmount =
            convertNumberToOnlyTwoDecimals(
                expenditureInstallment.getPendingAmount() - expenditureInstallment.getInstallmentAmount()
            );
        expenditureInstallment.setPendingAmount(newPendingAmount);
        if (newPendingAmount < 1) {
          expenditureInstallment.setFullPaid(true);
        }
        expenditureInstallmentRepository.save(expenditureInstallment);
      }
    }
    return "The sync was successful";
  }

  /**
   * Build entities Expenditure and MailMessage
   *
   * @param msg          the msg
   * @param subCategory  the sub category
   * @param expenditures the expenditures
   * @param mailMessages the mail messages
   * @return payee from the mail message
   * @throws NoSuchAlgorithmException the no such algorithm exception
   */
  public String buildEntitiesAndGetPayee(MessageDto msg, SubCategory subCategory,
                                         List<Expenditure> expenditures,
                                         List<MailMessage> mailMessages) throws NoSuchAlgorithmException {
    var transactionDate = convertDatetimeToUTCMinusFive(msg.getCreatedDateTime());
    var referenceId = Utilities.generateSha256FromMailIdOrPayee(transactionDate, msg.getId());
    var bodyTextContent = ExpenditureExtractorUtil.convertHTMLTextToPlainText(msg.getBody());
    var subject = msg.getSubject();
    var payee = ExpenditureExtractorUtil.extractThePayeeTrim(subject, bodyTextContent);
    if (payee != null) {
      Integer subCategoryId = payeeCategorizationService.obtainSubCategoryByPayee(payee);
      if (Objects.nonNull(subCategoryId)) {
        subCategory = subCategoryRepository.findByIdWithCategory(subCategoryId).orElse(subCategory);
        if (DEFAULT_EXPENDITURE_CATEGORY.equals(subCategory.getCategory().getName())) {
          subCategory = updateSubCategoryIfFindCoincidence(payee, subCategory);
        }
      } else {
        subCategory = updateSubCategoryIfFindCoincidence(payee, subCategory);
      }
    }

    if(Objects.isNull(payee)) {
      payee = "Payee not found";
    }
    Expenditure expenditure = expenditureMapper(msg, subCategory, referenceId, subject, payee,
        bodyTextContent);
    expenditures.add(expenditure);
    mailMessages.add(mailMessageMapper(msg, referenceId, bodyTextContent));
    return expenditure.getPayee();
  }

  public Expenditure expenditureMapper(MessageDto msg, SubCategory subCategory,
                                              String referenceId, String subject, String payee,
                                              String bodyTextContent) {
    var currency = MailRegex.extractExpenditureCurrency(bodyTextContent);
    var amount = extractExpenditureAmount(bodyTextContent);
    Double conversionToPen = null;
    if(currency == Expenditure.Currency.USD) {
      var usdExchangeRate = appDataConfiguration.getExchangeRateData().get(AppDataConfiguration.CURRENT_USD_EXCHANGE_RATE).getUsdExchangeRate();
      conversionToPen = usdExchangeRate.doubleValue() * amount;
    }
    return Expenditure.builder().referenceId(referenceId).description(subject)
        .transactionDate(convertDatetimeToUTCMinusFive(msg.getCreatedDateTime()))
        .payee(payee)
        .currency(currency)
        .amount(amount)
        .conversionToPen(convertNumberToOnlyTwoDecimals(conversionToPen))
        .shared(false)
        .sharedAmount(null)
        .singlePayment(true)
        .installments((short) 1).lent(false)
        .lentTo(null)
        .loanState(null)
        .loanAmount(null)
        .wasBorrowed(false)
        .borrowedFrom(null)
        .borrowedState(null)
        .recurrent(false)
        .forDaughter(false)
        .expImported(false)
        .subCategory(subCategory)
        .build();
  }

  public static MailMessage mailMessageMapper(MessageDto msg, String referenceId,
                                              String bodyTextContent) {

    return MailMessage.builder().id(msg.getId()).createdDateTime(convertDatetimeToUTCMinusFive(msg.getCreatedDateTime())).subject(msg.getSubject()).bodyPreview(msg.getBodyPreview()).bodyHtml(msg.getBody().getContent()).bodyTextContent(bodyTextContent).fromEmail(msg.getFrom().getEmailAddress().getName()).webLink(msg.getWebLink()).referenceId(referenceId).build();
  }

  private SubCategory updateSubCategoryIfFindCoincidence(String payee, SubCategory subCategory) {
    var optCoincidence =
        payeeCoincidenceRepository.findAllWithSubCategory().stream().filter(pc -> payee.toLowerCase().contains(pc.getPartialPayeeName().toLowerCase())).findFirst();
    if (optCoincidence.isPresent()) {
      return optCoincidence.get().getSubCategory();
    }
    return subCategory;
  }
}

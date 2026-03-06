package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.advice.CustomValidationException;
import com.bindord.financemanagement.config.AppDataConfiguration;
import com.bindord.financemanagement.model.ParameterUserId;
import com.bindord.financemanagement.model.auth.User;
import com.bindord.financemanagement.model.finance.Category;
import com.bindord.financemanagement.model.finance.Expenditure;
import com.bindord.financemanagement.model.finance.ExpenditureInstallment;
import com.bindord.financemanagement.model.finance.RecurrentExpenditure;
import com.bindord.financemanagement.model.finance.SubCategory;
import com.bindord.financemanagement.model.mapper.MailMessageMapper;
import com.bindord.financemanagement.model.record.ProviderMailMessage;
import com.bindord.financemanagement.model.source.MailExclusionRule;
import com.bindord.financemanagement.model.source.MailMessage;
import com.bindord.financemanagement.model.source.ParameterUser;
import com.bindord.financemanagement.repository.CategoryRepository;
import com.bindord.financemanagement.repository.ExpenditureInstallmentRepository;
import com.bindord.financemanagement.repository.ExpenditureRepository;
import com.bindord.financemanagement.repository.MailExclusionRuleRepository;
import com.bindord.financemanagement.repository.MailMessageRepository;
import com.bindord.financemanagement.repository.ParameterUserRepository;
import com.bindord.financemanagement.repository.PayeeCoincidenceRepository;
import com.bindord.financemanagement.repository.RecurrentExpenditureRepository;
import com.bindord.financemanagement.repository.SubCategoryRepository;
import com.bindord.financemanagement.resolver.MailProviderResolver;
import com.bindord.financemanagement.svc.auth.CurrentUserService;
import com.bindord.financemanagement.utils.ExpenditureExtractorUtil;
import com.bindord.financemanagement.utils.MailRegex;
import com.bindord.financemanagement.utils.Utilities;
import com.bindord.financemanagement.utils.enums.Currency;
import com.bindord.financemanagement.utils.enums.MailProvider;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
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

  private static final String LAST_SYNC_DATE_FOR_OUTLOOK = "LAST_SYNC_DATE_FOR_OUTLOOK";
  private static final String LAST_SYNC_DATE_FOR_GMAIL = "LAST_SYNC_DATE_FOR_GMAIL";
  private static final String LAST_RECURRENTS_SYNC_DATE = "LAST_RECURRENT_SYNC_DATE";
  private static final String MAIL_PROVIDER = "MAIL_PROVIDER";


  private final MailExclusionRuleRepository mailExclusionRuleRepository;
  private final MailProviderResolver mailProviderResolver;
  private final ParameterUserRepository parameterUserRepository;
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
  private final Validator validator;
  private final CurrentUserService currentUserService;
  private final MailMessageMapper mailMessageMapper;

  /**
   * Execute synchronization from outlook to expenditure table
   *
   * @param accessToken token associated to Microsoft Graph API
   */
  @Override
  public String executeSynchronization(String accessToken, MailProvider provider) throws Exception {

    var currentUserId = currentUserService.getCurrentUserId();

    MailProviderFacade mailProviderFacade = mailProviderResolver.resolve(provider);

    List<MailExclusionRule> exclusionsList = mailExclusionRuleRepository.findAll();
    Set<String> exclusions =
        exclusionsList.stream().map(MailExclusionRule::getKeyword).collect(Collectors.toSet());
    String lastSyncKey =
        provider == MailProvider.GMAIL
            ? LAST_SYNC_DATE_FOR_GMAIL
            : LAST_SYNC_DATE_FOR_OUTLOOK;

    ParameterUser parameter =
        parameterUserRepository.findById(new ParameterUserId(currentUserId, lastSyncKey))
            .orElseThrow(() -> new Exception(
                "Error while trying to get the last sync date from parameters_users table"));

    if (!parameter.isEnabled()) {
      throw new CustomValidationException("Parameter disabled: " + (provider == MailProvider.GMAIL ? LAST_SYNC_DATE_FOR_GMAIL : LAST_SYNC_DATE_FOR_OUTLOOK));
    }

    String paramLastSyncDateTime = parameter.getValue();
    if (paramLastSyncDateTime == null) {
      paramLastSyncDateTime =
          OffsetDateTime.now(ZoneOffset.UTC).minusMonths(6).withNano(0).toString();
    }

    OffsetDateTime lastSyncDateTime = OffsetDateTime.parse(paramLastSyncDateTime);
    List<ProviderMailMessage> beforeFilterMessages =
        mailProviderFacade.findMessagesSince(accessToken,
            lastSyncDateTime.toLocalDateTime().plusMinutes(300).plusSeconds(1));

    if (beforeFilterMessages.isEmpty()) {
      var msg = "There is no record to register. <a href='/expenditure/list'>Go to Expenditure " +
          "List</a>";
      log.info(msg);
      return msg;
    }

    List<ProviderMailMessage> postFilterMessages =
        Utilities.getFilteredMessages(beforeFilterMessages,
        exclusions);

    if (postFilterMessages.isEmpty()) {
      var msg = "After filtering... no record to register was found. <a " +
          "href='/expenditure/list'>Go to Expenditure List</a>";
      log.info(msg);
      return msg;
    }


    //Preparation
    Category category = categoryRepository.findByName(DEFAULT_EXPENDITURE_CATEGORY);
    SubCategory subCategory = subCategoryRepository.findByCategoryIdAndName(category.getId(),
        DEFAULT_EXPENDITURE_CATEGORY);
    subCategory.setCategory(category);
    List<Expenditure> expenditures = new ArrayList<>();
    List<MailMessage> mailMessages = new ArrayList<>();

    // Parse the string to OffsetDateTime
    OffsetDateTime lastRecordDateTime =
        OffsetDateTime.parse(postFilterMessages.getLast().createdDateTime());
    // Subtract 5 hours
    OffsetDateTime updatedLastRecordDateTime = lastRecordDateTime.minusHours(5);
    String lastMessageDateTime = updatedLastRecordDateTime.toString();

    for (ProviderMailMessage msg : postFilterMessages) {
      String payee = buildEntitiesAndGetPayee(msg, subCategory, expenditures, mailMessages);

      payeeCategorizationService.managePayeeCategorization(payee, subCategory.getId());
    }

    expenditures.forEach(expenditure -> {
      expenditure.setUser(
          User.builder()
              .userId(currentUserId)
              .build());
      Set<ConstraintViolation<Expenditure>> violations =
          validator.validate(expenditure);
      if (!violations.isEmpty()) {
        log.error("Obj error: {}, {}, {}", expenditure.getPayee(), expenditure.getDescription(),
            expenditure.getTransactionDate());
        log.error("Validate the mail's subject and review your mail exclusion rules. Add new " +
            "rules if necessary");
        throw new ConstraintViolationException(violations);
      }
    });

    expenditureRepository.saveAll(expenditures);
    mailMessageRepository.saveAll(mailMessages);

    ParameterUser paramRecurrents =
        parameterUserRepository.findById(new ParameterUserId(currentUserId,
                LAST_RECURRENTS_SYNC_DATE))
            .orElseThrow(() -> new CustomValidationException(
                "Error while trying to get the last sync date from parameters_users table"));

    if (paramRecurrents.isEnabled() &&
        (paramRecurrents.getValue() == null || updatedLastRecordDateTime.getMonth() != OffsetDateTime.parse(paramRecurrents.getValue()).getMonth())) {

      List<RecurrentExpenditure> recurrents =
          recurrentExpenditureRepository.findAllEnabledWithSubCategoryAndCatByUserId(currentUserId);
      if (recurrents.isEmpty()) {
        log.info("There are no recurrent expenditures to sync");
      } else {
        paramRecurrents.setValue(lastMessageDateTime);
        parameterUserRepository.save(paramRecurrents);

        List<Expenditure> recurrentExpenditureList = new ArrayList<>();
        for (RecurrentExpenditure reccExpend : recurrents) {
          recurrentExpenditureList.add(expenditureService.expenditureMapperFromRecurrentExpenditure(reccExpend));
        }
        expenditureRepository.saveAll(recurrentExpenditureList);
      }
    }
    parameter.setValue(lastMessageDateTime);
    parameterUserRepository.save(parameter);

    List<ExpenditureInstallment> expenditureInstallments =
        expenditureInstallmentRepository.findAllByUserIdAndFullPaidIsFalse(currentUserId);

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
    return "The sync was successful. <a href='/'>Go to home</a>";
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
  public String buildEntitiesAndGetPayee(ProviderMailMessage msg, SubCategory subCategory,
                                         List<Expenditure> expenditures,
                                         List<MailMessage> mailMessages) throws NoSuchAlgorithmException {
    var transactionDate = convertDatetimeToUTCMinusFive(msg.createdDateTime());
    var referenceId = Utilities.generateSha256FromMailIdOrPayee(transactionDate, msg.id());
    var bodyTextContent = ExpenditureExtractorUtil.convertHTMLTextToPlainText(msg.bodyHtml());
    var subject = msg.subject();
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

    if (Objects.isNull(payee)) {
      payee = "Payee not found";
    }
    Expenditure expenditure = expenditureMapper(msg, subCategory, referenceId, subject, payee,
        bodyTextContent);
    expenditures.add(expenditure);
    mailMessages.add(mailMessageMapper(msg, referenceId, bodyTextContent));
    return expenditure.getPayee();
  }

  public Expenditure expenditureMapper(ProviderMailMessage msg, SubCategory subCategory,
                                       String referenceId, String subject, String payee,
                                       String bodyTextContent) {
    var currency = MailRegex.extractExpenditureCurrency(bodyTextContent);
    var amount = extractExpenditureAmount(bodyTextContent);
    Double conversionToPen = null;
    if (currency == Currency.USD) {
      var usdExchangeRate =
          appDataConfiguration.getExchangeRateData().get(AppDataConfiguration.CURRENT_USD_EXCHANGE_RATE).getUsdExchangeRate();
      conversionToPen = usdExchangeRate.doubleValue() * amount;
    }
    return Expenditure.builder().referenceId(referenceId).description(subject)
        .transactionDate(convertDatetimeToUTCMinusFive(msg.createdDateTime()))
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

  public static MailMessage mailMessageMapper(ProviderMailMessage msg, String referenceId,
                                              String bodyTextContent) {
    return MailMessage.builder().id(msg.id()).createdDateTime(convertDatetimeToUTCMinusFive(msg.createdDateTime())).subject(msg.subject()).bodyPreview(msg.bodyPreview()).bodyHtml(msg.bodyHtml()).bodyTextContent(bodyTextContent).fromEmail(msg.from()).webLink(msg.webLink()).referenceId(referenceId).build();
  }

  private SubCategory updateSubCategoryIfFindCoincidence(String payee, SubCategory subCategory) {
    var optCoincidence =
        payeeCoincidenceRepository.findAllWithSubCategory().stream().filter(pc -> payee.toLowerCase().contains(pc.getPartialPayeeName().trim().toLowerCase())).findFirst();
    if (optCoincidence.isPresent()) {
      return optCoincidence.get().getSubCategory();
    }
    return subCategory;
  }
}

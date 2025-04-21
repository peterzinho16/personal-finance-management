package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.model.finance.Category;
import com.bindord.financemanagement.model.finance.ExpenditureOthers;
import com.bindord.financemanagement.model.finance.SubCategory;
import com.bindord.financemanagement.model.source.GmailMessageDto;
import com.bindord.financemanagement.model.source.MailExclusionRule;
import com.bindord.financemanagement.model.source.MailMessage;
import com.bindord.financemanagement.model.source.Parameter;
import com.bindord.financemanagement.repository.CategoryRepository;
import com.bindord.financemanagement.repository.ExpenditureOthersRepository;
import com.bindord.financemanagement.repository.MailExclusionRuleRepository;
import com.bindord.financemanagement.repository.MailMessageRepository;
import com.bindord.financemanagement.repository.ParameterRepository;
import com.bindord.financemanagement.repository.PayeeCoincidenceRepository;
import com.bindord.financemanagement.repository.SubCategoryRepository;
import com.bindord.financemanagement.utils.ExpenditureExtractorUtil;
import com.bindord.financemanagement.utils.MailRegex;
import com.bindord.financemanagement.utils.Utilities;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.bindord.financemanagement.utils.Constants.DEFAULT_EXPENDITURE_CATEGORY;
import static com.bindord.financemanagement.utils.MailRegex.extractExpenditureAmount;
import static com.bindord.financemanagement.utils.Utilities.convertDatetimeToUTCMinusFive;

@Slf4j
@AllArgsConstructor
@Service
public class ExpenditureOthersSyncServiceImpl implements ExpenditureOthersSyncService {

  private static final String LAST_SYNC_DATE_FOR_GMAIL = "LAST_SYNC_DATE_FOR_GMAIL";


  private final MailExclusionRuleRepository mailExclusionRuleRepository;
  private final GmailFacade emailFacade;
  private final ParameterRepository parameterRepository;
  private final ExpenditureOthersRepository expenditureRepository;
  private final CategoryRepository categoryRepository;
  private final SubCategoryRepository subCategoryRepository;
  private final MailMessageRepository mailMessageRepository;
  private final PayeeCategorizationService payeeCategorizationService;
  private final PayeeCoincidenceRepository payeeCoincidenceRepository;

  /**
   * Execute synchronization from gmail to expenditure table
   */
  @Override
  public String executeSynchronization() throws Exception {

    List<MailExclusionRule> exclusionsList = mailExclusionRuleRepository.findAll();
    Set<String> exclusions =
        exclusionsList.stream().map(MailExclusionRule::getKeyword).collect(Collectors.toSet());
    Parameter parameter =
        parameterRepository.findById(LAST_SYNC_DATE_FOR_GMAIL).orElseThrow(() -> new Exception(
            "Error " +
            "while" + " trying to get the last sync date from parameters table"));
    String paramLastSyncDateTime = parameter.getValue();
    OffsetDateTime lastSyncDateTime = OffsetDateTime.parse(paramLastSyncDateTime);
    List<GmailMessageDto> beforeFilterMessages =
        emailFacade.findByCreatedDateTimeGreaterThan(
            lastSyncDateTime.toLocalDateTime().plusMinutes(300).plusSeconds(1));

    if (beforeFilterMessages.isEmpty()) {
      var msg = "There is no record to register";
      log.info(msg);
      return msg;
    }

    List<GmailMessageDto> postFilterMessages =
        Utilities.getFilteredGmailMessages(beforeFilterMessages,
            exclusions);

    //Preparation
    Category category = categoryRepository.findByName(DEFAULT_EXPENDITURE_CATEGORY);
    SubCategory subCategory = subCategoryRepository.findByCategoryIdAndName(category.getId(),
        DEFAULT_EXPENDITURE_CATEGORY);
    subCategory.setCategory(category);
    List<ExpenditureOthers> expenditures = new ArrayList<>();
    List<MailMessage> mailMessages = new ArrayList<>();

    if(postFilterMessages.isEmpty()) {
      return "No records were found to sync after filtering.";
    }

    for (GmailMessageDto msg : postFilterMessages) {
      String payee = buildEntitiesAndGetPayee(msg, subCategory, expenditures, mailMessages);
      payeeCategorizationService.managePayeeCategorization(payee, subCategory.getId());
    }

    expenditureRepository.saveAll(expenditures);
    mailMessageRepository.saveAll(mailMessages);

    String lastMessageDateTime = Utilities.getMaxTransactionDate(expenditures).toString();
    parameter.setValue(lastMessageDateTime + "Z");
    parameterRepository.save(parameter);
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
  public String buildEntitiesAndGetPayee(GmailMessageDto msg, SubCategory subCategory,
                                         List<ExpenditureOthers> expenditures,
                                         List<MailMessage> mailMessages) throws NoSuchAlgorithmException {
    var transactionDate = convertDatetimeToUTCMinusFive(msg.getCreatedDateTime());
    var referenceId = Utilities.generateSha256FromMailIdOrPayee(transactionDate, msg.getId());
    var bodyTextContent = ExpenditureExtractorUtil.convertHTMLTextToPlainText(msg.getBodyHTML());
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
    ExpenditureOthers expenditure = expenditureMapper(msg, subCategory, referenceId, subject, payee,
        bodyTextContent);
    expenditures.add(expenditure);
    mailMessages.add(mailMessageMapper(msg, referenceId, bodyTextContent));
    return expenditure.getPayee();
  }

  public static ExpenditureOthers expenditureMapper(GmailMessageDto msg, SubCategory subCategory,
                                                    String referenceId, String subject,
                                                    String payee,
                                                    String bodyTextContent) {
    return ExpenditureOthers.builder().referenceId(referenceId).description(subject)
        .transactionDate(convertDatetimeToUTCMinusFive(msg.getCreatedDateTime()))
        .payee(payee)
        .currency(MailRegex.extractExpenditureCurrency(bodyTextContent))
        .amount(extractExpenditureAmount(bodyTextContent))
        .singlePayment(true)
        .subCategory(subCategory)
        .build();
  }

  public static MailMessage mailMessageMapper(GmailMessageDto msg, String referenceId,
                                              String bodyTextContent) {

    return MailMessage.builder().id(msg.getId()).createdDateTime(convertDatetimeToUTCMinusFive(msg.getCreatedDateTime())).subject(msg.getSubject()).bodyPreview(msg.getBodyPreview()).bodyHtml(msg.getBodyHTML()).bodyTextContent(bodyTextContent).fromEmail(msg.getFrom()).webLink(msg.getWebLink()).referenceId(referenceId).build();
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

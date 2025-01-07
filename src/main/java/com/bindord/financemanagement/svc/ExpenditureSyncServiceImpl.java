package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.model.finance.Category;
import com.bindord.financemanagement.model.finance.Expenditure;
import com.bindord.financemanagement.model.finance.SubCategory;
import com.bindord.financemanagement.model.source.MailExclusionRule;
import com.bindord.financemanagement.model.source.MailMessage;
import com.bindord.financemanagement.model.source.MessageDto;
import com.bindord.financemanagement.model.source.Parameter;
import com.bindord.financemanagement.repository.CategoryRepository;
import com.bindord.financemanagement.repository.ExpenditureRepository;
import com.bindord.financemanagement.repository.MailExclusionRuleRepository;
import com.bindord.financemanagement.repository.MailMessageRepository;
import com.bindord.financemanagement.repository.ParameterRepository;
import com.bindord.financemanagement.repository.SubCategoryRepository;
import com.bindord.financemanagement.utils.Constants;
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

import static com.bindord.financemanagement.utils.MailRegex.extractExpenditureAmount;
import static com.bindord.financemanagement.utils.Utilities.convertDatetimeToUTCMinusFive;

@Slf4j
@AllArgsConstructor
@Service
public class ExpenditureSyncServiceImpl implements ExpenditureSyncService {

  private static final String LAST_SYNC_DATE = "LAST_SYNC_DATE";


  private final MailExclusionRuleRepository mailExclusionRuleRepository;
  private final EmailFacade emailFacade;
  private final ParameterRepository parameterRepository;
  private final ExpenditureRepository expenditureRepository;
  private final CategoryRepository categoryRepository;
  private final SubCategoryRepository subCategoryRepository;
  private final MailMessageRepository mailMessageRepository;
  private final PayeeCategorizationService payeeCategorizationService;

  /**
   * Execute synchronization from outlook to expenditure table
   *
   * @param accessToken token associated to Microsoft Graph API
   */
  @Override
  public String executeSynchronization(String accessToken) throws Exception {

    List<MailExclusionRule> exclusionsList = mailExclusionRuleRepository.findAll();
    Set<String> exclusions = exclusionsList
        .stream()
        .map(MailExclusionRule::getKeyword)
        .collect(Collectors.toSet());
    Parameter parameter = parameterRepository.findById(LAST_SYNC_DATE)
        .orElseThrow(() -> new Exception(
            "Error while trying to get the last sync date from parameters table"));
    String lastSyncDateTime = parameter.getValue();
    OffsetDateTime offsetDateTime = OffsetDateTime.parse(lastSyncDateTime);
    List<MessageDto> beforeFilterMessages = emailFacade
        .findByCreatedDateTimeGreaterThan(accessToken,
            offsetDateTime.toLocalDateTime().plusMinutes(300).plusSeconds(1)
        );

    if (beforeFilterMessages.isEmpty()) {
      var msg = "There is no record to register";
      log.info(msg);
      return msg;
    }

    List<MessageDto> postFilterMessages = Utilities.getFilteredMessages(beforeFilterMessages,
        exclusions);


    //Preparation
    Category category = categoryRepository.findByName(Constants.DEFAULT_EXPENDITURE_CATEGORY);
    SubCategory subCategory = subCategoryRepository.findByCategoryIdAndName(category.getId(),
        Constants.DEFAULT_EXPENDITURE_CATEGORY);
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
    parameter.setValue(lastMessageDateTime);
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
  public String buildEntitiesAndGetPayee(MessageDto msg, SubCategory subCategory,
                                         List<Expenditure> expenditures,
                                         List<MailMessage> mailMessages) throws NoSuchAlgorithmException {
    var transactionDate = convertDatetimeToUTCMinusFive(msg.getCreatedDateTime());
    var referenceId = Utilities.generateSha256FromMailContent(transactionDate, msg.getId());
    var bodyTextContent = ExpenditureExtractorUtil.convertHTMLTextToPlainText(msg.getBody());
    var subject = msg.getSubject();
    var payee = ExpenditureExtractorUtil.extractThePayeeTrim(subject, bodyTextContent);
    if (payee != null) {
      Integer subCategoryId = payeeCategorizationService.obtainSubCategoryByPayee(payee);
      if (Objects.nonNull(subCategoryId)) {
        subCategory = subCategoryRepository.findById(subCategoryId).orElse(subCategory);
      }
    }
    Expenditure expenditure = expenditureMapper(msg, subCategory, referenceId, subject, payee,
        bodyTextContent);
    expenditures.add(expenditure);
    mailMessages.add(mailMessageMapper(msg, referenceId, bodyTextContent));
    return expenditure.getPayee();
  }

  public static Expenditure expenditureMapper(MessageDto msg, SubCategory subCategory,
                                              String referenceId, String subject, String payee,
                                              String bodyTextContent) {
    return Expenditure.builder()
        .referenceId(referenceId)
        .description(subject)
        .transactionDate(
            convertDatetimeToUTCMinusFive(msg.getCreatedDateTime())
        )
        .payee(payee)
        .currency(MailRegex.extractExpenditureCurrency(bodyTextContent))
        .amount(
            extractExpenditureAmount(
                bodyTextContent
            )
        )
        .shared(false)
        .sharedAmount(null)
        .singlePayment(true)
        .installments((short) 1)
        .lent(false)
        .lentTo(null)
        .loanState(null)
        .loanAmount(null)
        .recurrent(false)
        .subCategory(subCategory)
        .build();
  }

  public static MailMessage mailMessageMapper(MessageDto msg, String referenceId,
                                              String bodyTextContent) {

    return MailMessage.builder()
        .id(msg.getId())
        .createdDateTime(
            convertDatetimeToUTCMinusFive(msg.getCreatedDateTime())
        )
        .subject(msg.getSubject())
        .bodyPreview(msg.getBodyPreview())
        .bodyHtml(msg.getBody().getContent())
        .bodyTextContent(bodyTextContent)
        .fromEmail(msg.getFrom().getEmailAddress().getName())
        .webLink(msg.getWebLink())
        .referenceId(referenceId)
        .build();
  }
}

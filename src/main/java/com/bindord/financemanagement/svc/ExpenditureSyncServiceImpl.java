package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.HTMLTextExtractor;
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
import java.util.Set;
import java.util.stream.Collectors;

import static com.bindord.financemanagement.utils.MailRegex.extractExpenditureAmount;
import static com.bindord.financemanagement.utils.Utilities.convertDatetimeToUTCMinusFive;

@Slf4j
@AllArgsConstructor
@Service
public class ExpenditureSyncServiceImpl implements ExpenditureSyncService {

  private static final String LAST_SYNC_DATE = "LAST_SYNC_DATE";
  private final String INPUT_NOT_FOUND = "Not found";


  private final MailExclusionRuleRepository mailExclusionRuleRepository;
  private final EmailFacade emailFacade;
  private final ParameterRepository parameterRepository;
  private final ExpenditureRepository expenditureRepository;
  private final CategoryRepository categoryRepository;
  private final SubCategoryRepository subCategoryRepository;
  private final MailMessageRepository mailMessageRepository;

  /**
   * Execute synchronization from outlook to expenditure table
   *
   * @param accessToken token associated to Microsoft Graph API
   */
  @Override
  public void executeSynchronization(String accessToken) throws Exception {

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
      log.info("There is no record to register");
      return;
    }

    List<MessageDto> postFilterMessages = Utilities.getFilteredMessages(beforeFilterMessages, exclusions);


    //Preparation
    Category category = categoryRepository.findByName(Constants.DEFAULT_EXPENDITURE_CATEGORY);
    SubCategory subCategory = subCategoryRepository.findByCategoryIdAndName(category.getId(), Constants.DEFAULT_EXPENDITURE_CATEGORY);
    List<Expenditure> expenditures = new ArrayList<>();
    List<MailMessage> mailMessages = new ArrayList<>();

    // Parse the string to OffsetDateTime
    OffsetDateTime lastRecordDateTime = OffsetDateTime.parse(postFilterMessages.getLast().getCreatedDateTime());
    // Subtract 5 hours
    OffsetDateTime updatedLastRecordDateTime = lastRecordDateTime.minusHours(5);
    String lastMessageDateTime = updatedLastRecordDateTime.toString();

    for (MessageDto msg : postFilterMessages) {
      buildingEntities(msg, subCategory, expenditures, mailMessages);
    }
    expenditureRepository.saveAll(expenditures);
    mailMessageRepository.saveAll(mailMessages);
    parameter.setValue(lastMessageDateTime);
    parameterRepository.save(parameter);
  }

  private void buildingEntities(MessageDto msg, SubCategory subCategory, List<Expenditure> expenditures, List<MailMessage> mailMessages) throws NoSuchAlgorithmException {
    var transactionDate = convertDatetimeToUTCMinusFive(msg.getCreatedDateTime());
    var referenceId = Utilities.generateSha256FromMailContent(transactionDate, msg.getId());
    var bodyTextContent = convertHTMLTextToPlainText(msg.getBody());
    var subject = msg.getSubject();
    Expenditure expenditure = Expenditure.builder()
        .referenceId(referenceId)
        .description(subject)
        .transactionDate(
            convertDatetimeToUTCMinusFive(msg.getCreatedDateTime())
        )
        .payee(ExpenditureExtractorUtil.extractThePayeeTrim(subject, bodyTextContent))
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
        .lentTo(null)
        .loanState(null)
        .loanAmount(null)
        .recurrent(false)
        .subCategory(subCategory)
        .build();
    expenditures.add(expenditure);

    MailMessage mailMessage = MailMessage.builder()
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
    mailMessages.add(mailMessage);
  }

  private String convertHTMLTextToPlainText(MessageDto.Body body) {
    return HTMLTextExtractor.extractTextJsoup(body.getContent());
  }
}

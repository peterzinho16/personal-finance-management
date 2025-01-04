package com.bindord.financemanagement.controller.expend;

import com.bindord.financemanagement.model.finance.Category;
import com.bindord.financemanagement.model.finance.Expenditure;
import com.bindord.financemanagement.model.finance.MicrosoftAccessToken;
import com.bindord.financemanagement.model.finance.SubCategory;
import com.bindord.financemanagement.model.source.MailExclusionRule;
import com.bindord.financemanagement.model.source.MailMessage;
import com.bindord.financemanagement.model.source.MailMessagesResponse;
import com.bindord.financemanagement.model.source.MessageDto;
import com.bindord.financemanagement.repository.CategoryRepository;
import com.bindord.financemanagement.repository.ExpenditureRepository;
import com.bindord.financemanagement.repository.MailExclusionRuleRepository;
import com.bindord.financemanagement.repository.MailMessageRepository;
import com.bindord.financemanagement.repository.PayeeCategorizationRepository;
import com.bindord.financemanagement.repository.SubCategoryRepository;
import com.bindord.financemanagement.repository.external.MicrosoftGraphClient;
import com.bindord.financemanagement.utils.Constants;
import com.bindord.financemanagement.utils.ExpenditureExtractorUtil;
import com.bindord.financemanagement.utils.MailRegex;
import com.bindord.financemanagement.utils.Utilities;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.bindord.financemanagement.utils.MailRegex.extractExpenditureAmount;
import static com.bindord.financemanagement.utils.Utilities.convertDatetimeToUTCMinusFive;

@Slf4j
@Controller
@RestController
@RequestMapping("/eureka/finance-app/automatic-ingest")
@AllArgsConstructor
public class MassiveIngestController {

  private final ExpenditureRepository expenditureRepository;
  private final SubCategoryRepository subCategoryRepository;
  private final CategoryRepository categoryRepository;
  private final MicrosoftGraphClient microsoftGraphClient;
  private final MailMessageRepository mailMessageRepository;
  private final MailExclusionRuleRepository mailExclusionRuleRepository;
  private final PayeeCategorizationRepository payeeCategorizationRepository;

  @Transactional
  @GetMapping(value = "/full", produces = MediaType.APPLICATION_JSON_VALUE)
  public String ingestToDatabaseWithAllMessages(HttpSession session,
                                                @RequestParam(required = false) String withValidation) throws Exception {
    int batchSize = 100;
    int currentSkip = 0;
    MicrosoftAccessToken token =
        (MicrosoftAccessToken) session.getAttribute(Utilities.SESSION_TOKEN);
    MailMessagesResponse mailMessagesResponse =
        microsoftGraphClient.getMessages(Constants.INBOX_FOLDER_ID,
            Constants.NOTIF_COMPRAS_SUB_FOLDER_ID, batchSize, currentSkip, token.getAccessToken());

    Category category = categoryRepository.findByName(Constants.DEFAULT_EXPENDITURE_CATEGORY);
    SubCategory subCategory = subCategoryRepository.findByCategoryIdAndName(category.getId(),
        Constants.DEFAULT_EXPENDITURE_CATEGORY);

    List<MailExclusionRule> exclusionsList = mailExclusionRuleRepository.findAll();
    Set<String> exclusions = exclusionsList
        .stream()
        .map(MailExclusionRule::getKeyword)
        .collect(Collectors.toSet());


    while (Objects.nonNull(mailMessagesResponse.getOdataNextLink())) {
      List<Expenditure> expenditures = new ArrayList<>();
      List<MailMessage> mailMessages = new ArrayList<>();
      log.info("Current skip: {}", currentSkip);
      List<MessageDto> beforeFilterMessages = mailMessagesResponse.getValue();
      var postFilterMessages = Utilities.getFilteredMessages(beforeFilterMessages, exclusions);
      Set<String> referenceIds = new HashSet<>();
      for (MessageDto msg : postFilterMessages) {
        var bodyTextContent = ExpenditureExtractorUtil.convertHTMLTextToPlainText(msg.getBody());
        var payee = ExpenditureExtractorUtil.extractThePayeeTrim(msg.getSubject(), bodyTextContent);
        var transactionDate = convertDatetimeToUTCMinusFive(msg.getCreatedDateTime());
        var referenceId = Utilities.generateSha256FromMailContent(transactionDate, msg.getId());
        referenceIds.add(referenceId);
        Expenditure expenditure = Expenditure.builder()
            .referenceId(referenceId)
            .description(msg.getSubject())
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

        if (payee != null) {
          payeeCategorizationRepository.insertPayeeCategorizationAndDoNothingOnConflict(payee,
              LocalDateTime.now(),
              subCategory.getId());
        }

      }
      if (withValidation != null) {
        Set<String> expendituresQueries = expenditureRepository.findByReferenceIdIn(referenceIds);
        if (!expendituresQueries.isEmpty()) {
          log.info("Some records were stored before, in total: " + expendituresQueries.size());
          expenditures = expenditures.stream().filter(expend ->
                  !expendituresQueries.contains(expend.getReferenceId()))
              .collect(Collectors.toList());
          expenditureRepository.saveAll(expenditures);
          mailMessageRepository.saveAll(mailMessages);
        } else {
          expenditureRepository.saveAll(expenditures);
          mailMessageRepository.saveAll(mailMessages);
        }
      } else {
        expenditureRepository.saveAll(expenditures);
        mailMessageRepository.saveAll(mailMessages);
      }

      if (expenditures.isEmpty()) {
        log.info("No new records to save.");
      }
      currentSkip += batchSize;
      mailMessagesResponse = microsoftGraphClient.getMessages(Constants.INBOX_FOLDER_ID,
          Constants.NOTIF_COMPRAS_SUB_FOLDER_ID, batchSize, currentSkip, token.getAccessToken());
    }
    return "Process has finished";
  }
}

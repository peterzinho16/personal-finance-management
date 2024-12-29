package com.bindord.financemanagement.controller;

import com.bindord.financemanagement.HTMLTextExtractor;
import com.bindord.financemanagement.model.finance.Category;
import com.bindord.financemanagement.model.finance.Expenditure;
import com.bindord.financemanagement.model.finance.SubCategory;
import com.bindord.financemanagement.model.source.MailMessagesResponse;
import com.bindord.financemanagement.model.source.MessageDto;
import com.bindord.financemanagement.repository.CategoryRepository;
import com.bindord.financemanagement.repository.ExpenditureRepository;
import com.bindord.financemanagement.repository.SubCategoryRepository;
import com.bindord.financemanagement.utils.Constants;
import com.bindord.financemanagement.utils.MailRegex;
import com.bindord.financemanagement.utils.Utilities;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.bindord.financemanagement.utils.MailRegex.extractExpenditureAmount;
import static com.bindord.financemanagement.utils.Utilities.convertDatetimeToUTCMinusFive;

@Slf4j
@Controller
@RestController
@RequestMapping("/eureka/finance-app/automatic-ingest")
@AllArgsConstructor
public class IngestToDatabaseController {

  private final ExpenditureRepository expenditureRepository;
  private final SubCategoryRepository subCategoryRepository;
  private final CategoryRepository categoryRepository;

  @Transactional
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Expenditure> ingestToDatabaseWithMailMessages() throws Exception {
    Resource resource = new ClassPathResource("mail-responses/response_last_10_mails.json");
    InputStream inputStream = resource.getInputStream();
    var objMapper = new ObjectMapper();
    objMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    MailMessagesResponse mailMessagesResponse = objMapper.readValue(inputStream, MailMessagesResponse.class);
    List<MessageDto> messages = mailMessagesResponse.getValue();
    List<Expenditure> expenditures = new ArrayList<>();
    Category category = categoryRepository.findByName(Constants.DEFAULT_EXPENDITURE_CATEGORY);
    SubCategory subCategory = subCategoryRepository.findByCategoryIdAndName(category.getId(), Constants.DEFAULT_EXPENDITURE_CATEGORY);
    Set<String> referenceIds = new HashSet<>();
    for (MessageDto msg : messages) {
//      System.out.println(msg.getBodyPreview());
//      System.out.println("***************************************************************************************");
//      System.out.println("***************************************************************************************");
      var transactionDate = convertDatetimeToUTCMinusFive(msg.getCreatedDateTime());
      var referenceId = Utilities.generateSha256FromMailContent(transactionDate, msg.getSubject());
      var bodyTextContet = convertHTMLTextToPlainText(msg.getBody());
      referenceIds.add(referenceId);
      Expenditure expenditure = Expenditure.builder()
          .referenceId(referenceId)
          .description(msg.getSubject())
          .transactionDate(
              convertDatetimeToUTCMinusFive(msg.getCreatedDateTime())
          )
          .currency(MailRegex.extractExpenditureCurrency(bodyTextContet))
          .amount(
              extractExpenditureAmount(
                  bodyTextContet
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

    }
    Set<String> expendituresQueries = expenditureRepository.findByReferenceIdIn(referenceIds);
    if (!expendituresQueries.isEmpty()) {
      log.info("Some or all records were stored before, in total: {}", expendituresQueries.size());
      expenditures = expenditures.stream().filter(expend ->
              !expendituresQueries.contains(expend.getReferenceId()))
          .collect(Collectors.toList());
    }

    if (expenditures.isEmpty()) {
      log.info("No new records to save.");
      return Collections.emptyList();
    }
    return expenditureRepository.saveAll(expenditures);
  }

  private String convertHTMLTextToPlainText(MessageDto.Body body) {
    return HTMLTextExtractor.extractTextJsoup(body.getContent());
  }
}

package com.bindord.financemanagement.controller;

import com.bindord.financemanagement.HTMLTextExtractor;
import com.bindord.financemanagement.model.source.MailMessagesResponse;
import com.bindord.financemanagement.model.source.MessageDto;
import com.bindord.financemanagement.repository.CategoryRepository;
import com.bindord.financemanagement.repository.ExpenditureRepository;
import com.bindord.financemanagement.repository.SubCategoryRepository;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.bindord.financemanagement.utils.Utilities.convertDatetimeToUTCMinusFive;

@Slf4j
@Controller
@RestController
@RequestMapping("/eureka/finance-app/generate-db")
@AllArgsConstructor
public class GenerateDatabaseController {

  private final ExpenditureRepository expenditureRepository;
  private final SubCategoryRepository subCategoryRepository;
  private final CategoryRepository categoryRepository;
  private final String INPUT_NOT_FOUND = "Not found";

  @Transactional
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public List<String> generateDatabaseFromMailMessages() throws Exception {
    Resource resource = new ClassPathResource("mail-responses/response_last_10_mails.json");
    InputStream inputStream = resource.getInputStream();
    var objMapper = new ObjectMapper();
    objMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    MailMessagesResponse mailMessagesResponse = objMapper.readValue(inputStream, MailMessagesResponse.class);
    List<MessageDto> messages = mailMessagesResponse.getValue();
    List<String> contactsOrBusinesses = new ArrayList<>();
    for (MessageDto msg : messages) {
      var transactionDate = convertDatetimeToUTCMinusFive(msg.getCreatedDateTime());
      var referenceId = Utilities.generateSha256FromMailContent(transactionDate, msg.getId());

      var bodyTextContet = convertHTMLTextToPlainText(msg.getBody());
      var subjectLwc = msg.getSubject().toLowerCase();
      //contactsOrBusinesses.add(bodyTextContet);
      if (subjectLwc.contains(EntitiesKeyword.YAPE.name().toLowerCase())) {
        var bodyParts = bodyTextContet.split("Nombre del Beneficiario");
        if (bodyParts.length < 2) {
          contactsOrBusinesses.add(INPUT_NOT_FOUND);
          continue;
        }
        String secondPart = bodyParts[1];

        var lastSplit = secondPart.split("Nº");

        if (lastSplit.length < 2) {
          contactsOrBusinesses.add(INPUT_NOT_FOUND);
          continue;
        }
        contactsOrBusinesses.add(msg.getSubject() + " | " + lastSplit[0]);

      }
      if (subjectLwc.contains(EntitiesKeyword.BCP.name().toLowerCase())) {
        var bodyParts = bodyTextContet.split("\\s+");
        int indexEmpresa = IntStream.range(0, bodyParts.length)
            .filter(i -> bodyParts[i].contains("Empresa"))
            .findFirst()
            .orElse(0);

        if (indexEmpresa == 0) {
          contactsOrBusinesses.add(INPUT_NOT_FOUND + " | " + msg.getSubject());
          continue;
        }

        int indexNumero = IntStream.range(indexEmpresa, bodyParts.length)
            .filter(ix -> bodyParts[ix].contains("Número"))
            .findFirst()
            .orElse(0);

        if (indexNumero == 0) {
          contactsOrBusinesses.add(INPUT_NOT_FOUND + " | " + msg.getSubject());
          continue;
        }
        String contactOrBusiness = Arrays.stream(bodyParts, indexEmpresa + 1, indexNumero)
            .collect(Collectors.joining(" "));
        contactsOrBusinesses.add(msg.getSubject() + " | " + contactOrBusiness);
      }

      if (subjectLwc.contains(EntitiesKeyword.DINERS.name().toLowerCase())) {

        var bodyParts = bodyTextContet.split("\\s+");
        int indexEmpresa = IntStream.range(0, bodyParts.length)
            .filter(i -> bodyParts[i].contains("comercio"))
            .findFirst()
            .orElse(0);

        if (indexEmpresa == 0) {
          contactsOrBusinesses.add(INPUT_NOT_FOUND + " | " + msg.getSubject());
          continue;
        }

        int indexNumero = IntStream.range(indexEmpresa, bodyParts.length)
            .filter(ix -> bodyParts[ix].contains("por"))
            .findFirst()
            .orElse(0);

        if (indexNumero == 0) {
          contactsOrBusinesses.add(INPUT_NOT_FOUND + " | " + msg.getSubject());
          continue;
        }

        var contactOrBusiness = Arrays.stream(bodyParts, indexEmpresa + 1, indexNumero)
            .collect(Collectors.joining(" "));
        contactsOrBusinesses.add(msg.getSubject() + " | " + contactOrBusiness);
      }

    }
    return contactsOrBusinesses
        .stream()
        .filter(str -> !str.toLowerCase().contains(INPUT_NOT_FOUND.toLowerCase()))
        .collect(Collectors.toList());
  }

  public enum EntitiesKeyword {
    YAPE, BCP, DINERS
  }

  private String convertHTMLTextToPlainText(MessageDto.Body body) {
    return HTMLTextExtractor.extractTextJsoup(body.getContent());
  }
}

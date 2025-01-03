package com.bindord.financemanagement.utils;

import com.bindord.financemanagement.HTMLTextExtractor;
import com.bindord.financemanagement.model.source.MessageDto;
import com.bindord.financemanagement.utils.Utilities.EntitiesKeyword;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class ExpenditureExtractorUtil {

  private static final String INPUT_NOT_FOUND = null;

  private static String extractThePayee(String subject, String bodyTextContent) {
    var subjectLwc = subject.toLowerCase();

    if (subjectLwc.contains(EntitiesKeyword.YAPE.name().toLowerCase())) {
      var bodyParts = bodyTextContent.split("Nombre del Beneficiario");
      if (bodyParts.length < 2) {
        return INPUT_NOT_FOUND;
      }
      String secondPart = bodyParts[1];

      var lastSplit = secondPart.split("Nº");

      if (lastSplit.length < 2) {
        return INPUT_NOT_FOUND;
      }
      return lastSplit[0];
    }
    if (subjectLwc.contains(EntitiesKeyword.BCP.name().toLowerCase())) {
      var bodyParts = bodyTextContent.split("\\s+");
      int indexEmpresa = IntStream.range(0, bodyParts.length)
          .filter(i -> bodyParts[i].contains("Empresa"))
          .findFirst()
          .orElse(0);

      if (indexEmpresa == 0) {
        return INPUT_NOT_FOUND;
      }

      int indexNumero = IntStream.range(indexEmpresa, bodyParts.length)
          .filter(ix -> bodyParts[ix].contains("Número"))
          .findFirst()
          .orElse(0);

      if (indexNumero == 0) {
        return INPUT_NOT_FOUND;
      }
      return Arrays.stream(bodyParts, indexEmpresa + 1, indexNumero)
          .collect(Collectors.joining(" "));
    }

    if (subjectLwc.contains(EntitiesKeyword.DINERS.name().toLowerCase())) {

      var bodyParts = bodyTextContent.split("\\s+");
      int indexEmpresa = IntStream.range(0, bodyParts.length)
          .filter(i -> bodyParts[i].contains("comercio"))
          .findFirst()
          .orElse(0);

      if (indexEmpresa == 0) {
        return INPUT_NOT_FOUND;
      }

      int indexNumero = IntStream.range(indexEmpresa, bodyParts.length)
          .filter(ix -> bodyParts[ix].contains("por"))
          .findFirst()
          .orElse(0);

      if (indexNumero == 0) {
        return INPUT_NOT_FOUND;
      }

      return Arrays.stream(bodyParts, indexEmpresa + 1, indexNumero)
          .collect(Collectors.joining(" "));
    }
    return INPUT_NOT_FOUND;
  }

  public static String extractThePayeeTrim(String subject, String bodyTextContent) {
    var result = extractThePayee(subject, bodyTextContent);
    return result != null ? result.trim() : null;
  }

  public static String convertHTMLTextToPlainText(MessageDto.Body body) {
    return HTMLTextExtractor.extractTextJsoup(body.getContent());
  }
}

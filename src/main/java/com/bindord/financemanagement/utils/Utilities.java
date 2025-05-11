package com.bindord.financemanagement.utils;

import com.bindord.financemanagement.model.finance.ExpenditureOthers;
import com.bindord.financemanagement.model.finance.MicrosoftAccessToken;
import com.bindord.financemanagement.model.source.GmailMessageDto;
import com.bindord.financemanagement.model.source.MessageDto;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class Utilities {

  private static final String SECURE_HASH = "SHA-256";
  public static final String SESSION_TOKEN = "sessionToken";
  public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

  public static LocalDateTime convertDatetimeToUTCMinusFive(String dateTimeString) {
    ZonedDateTime utcDateTime = ZonedDateTime.parse(dateTimeString,
        DateTimeFormatter.ISO_DATE_TIME);

    // Subtract 5 hours to get the new time in UTC-5
    ZonedDateTime dateTimeInUTCMinus5 = utcDateTime.minusHours(5);

    // Extract LocalDateTime (which does not contain timezone information)
    LocalDateTime localDateTime = dateTimeInUTCMinus5.toLocalDateTime();

    // Print the result
    log.debug("Converted LocalDateTime (UTC-5): {}", localDateTime);
    return localDateTime;
  }

  public static LocalDateTime getLocalDateTimeNowWithFormat() {
    // Get current LocalDateTime
    LocalDateTime now = LocalDateTime.now();

    // Define formatter with the correct pattern
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);

    // Format LocalDateTime to String
    String formattedDate = now.format(formatter);

    // Parse back to LocalDateTime
    return LocalDateTime.parse(formattedDate, formatter);
  }

  public static String generateSha256FromMailIdOrPayee(LocalDateTime transactionDate,
                                                       String mailId) throws NoSuchAlgorithmException {
    String timestamp = transactionDate.format(DateTimeFormatter.ISO_DATE_TIME);
    String flSting = timestamp + mailId;

    MessageDigest digest = MessageDigest.getInstance(SECURE_HASH);

    byte[] hashBytes = digest.digest(flSting.getBytes(StandardCharsets.UTF_8));

    // Convert byte array to hex string
    StringBuilder hexString = new StringBuilder();
    for (byte b : hashBytes) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) hexString.append('0'); // Ensure 2 characters
      hexString.append(hex);
    }

    return reduceHash(hexString.toString()).substring(0, 32);
  }

  private static String reduceHash(String fullHash) throws NoSuchAlgorithmException {
    // Hash the full SHA-256 hash again
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] reducedBytes = digest.digest(fullHash.getBytes(StandardCharsets.UTF_8));

    // Convert to hex and take the first 32 characters
    StringBuilder hexString = new StringBuilder();
    for (byte b : reducedBytes) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) hexString.append('0'); // Ensure 2 characters
      hexString.append(hex);
    }

    return hexString.toString().substring(0, 32);
  }

  public static boolean validateIfExistsValidSession(HttpSession session) {
    MicrosoftAccessToken obj = (MicrosoftAccessToken) session.getAttribute(SESSION_TOKEN);
    if (obj == null) {
      log.info("AccessToken is null");
    } else {
      log.info("Created at: {}", obj.getCreatedAt().toString());
    }
    return obj != null && obj.getExpiresAt().isAfter(LocalDateTime.now());
  }

  public static void storeSessionToken(HttpSession session, MicrosoftAccessToken accessToken) {
    session.setAttribute(SESSION_TOKEN, accessToken);
  }

  public static MicrosoftAccessToken retrieveSessionToken(HttpSession session) {
    if (validateIfExistsValidSession(session)) {
      return (MicrosoftAccessToken) session.getAttribute(SESSION_TOKEN);
    }
    return null;
  }

  public static List<MessageDto> getFilteredMessages(List<MessageDto> originalList,
                                                     Set<String> exclusions) {

    List<MessageDto> postFilterMessages = originalList.stream().filter(
        msg -> exclusions
            .stream()
            .noneMatch(
                ex -> msg.getSubject().toLowerCase().contains(ex)
            )
    ).toList();
    return postFilterMessages;
  }

  public static List<GmailMessageDto> getFilteredGmailMessages(List<GmailMessageDto> originalList,
                                                               Set<String> exclusions) {

    List<GmailMessageDto> postFilterMessages = originalList.stream().filter(
        msg -> exclusions
            .stream()
            .noneMatch(
                ex -> msg.getSubject().toLowerCase().contains(ex)
            )
    ).toList();
    return postFilterMessages;
  }

  public enum EntitiesKeyword {
    YAPE, BCP, DINERS, OH
  }

  public static double convertNumberToOnlyTwoDecimals(Double number) {
    if (number == null) {
      return 0.0;
    }
    // Create DecimalFormat for two decimal places
    DecimalFormat df = new DecimalFormat("#.00");
    // Format to a string
    String formattedNumber = df.format(number);
    // Convert back to double
    return Double.parseDouble(formattedNumber);
  }

  public static LocalDateTime getMaxTransactionDate(List<ExpenditureOthers> expenditures) {
    if (expenditures == null || expenditures.isEmpty()) {
      return null; // Or throw an exception if an empty list is not expected
    }

    Optional<LocalDateTime> maxDate = expenditures.stream()
        .map(ExpenditureOthers::getTransactionDate)
        .max(Comparator.naturalOrder());

    return maxDate.orElse(null); // Returns null if the list was empty
  }
}

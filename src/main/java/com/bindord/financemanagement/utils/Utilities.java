package com.bindord.financemanagement.utils;

import com.bindord.financemanagement.model.finance.MicrosoftAccessToken;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class Utilities {

  private static final String SECURE_HASH = "SHA-256";
  public static final String SESSION_TOKEN = "sessionToken";

  public static LocalDateTime convertDatetimeToUTCMinusFive(String dateTimeString) {
    ZonedDateTime utcDateTime = ZonedDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME);

    // Subtract 5 hours to get the new time in UTC-5
    ZonedDateTime dateTimeInUTCMinus5 = utcDateTime.minusHours(5);

    // Extract LocalDateTime (which does not contain timezone information)
    LocalDateTime localDateTime = dateTimeInUTCMinus5.toLocalDateTime();

    // Print the result
    log.debug("Converted LocalDateTime (UTC-5): {}", localDateTime);
    return localDateTime;
  }

  public static String generateSha256FromMailContent(LocalDateTime transactionDate, String mailId) throws NoSuchAlgorithmException {
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
    return obj != null && obj.getExpiresAt().isAfter(LocalDateTime.now());
  }

  public static void storeSessionToken(HttpSession session, MicrosoftAccessToken accessToken) {
    session.setAttribute(SESSION_TOKEN, accessToken);
  }
}

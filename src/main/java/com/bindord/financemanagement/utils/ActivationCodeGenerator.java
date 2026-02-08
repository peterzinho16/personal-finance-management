package com.bindord.financemanagement.utils;

import org.apache.commons.codec.digest.DigestUtils;

import java.security.SecureRandom;
import java.util.Base64;

public final class ActivationCodeGenerator {

  private static final SecureRandom RANDOM = new SecureRandom();

  public static String generateToken() {
    byte[] bytes = new byte[32]; // 256 bits
    RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  public static String hashToken(String token) {
    return DigestUtils.sha256Hex(token);
  }
}
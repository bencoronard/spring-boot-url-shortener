package dev.hireben.url_shortener.url.utility;

import java.security.SecureRandom;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UrlSafeIdGenerator {

  private final String urlSafeChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_";
  private final int charSetSize = urlSafeChars.length();
  private final SecureRandom random = new SecureRandom();

  // =============================================================================

  public String generateUrlSafeString(int length) {

    if (length <= 0) {
      throw new IllegalArgumentException("Length must be positive");
    }

    StringBuilder sb = new StringBuilder(length);

    for (int i = 0; i < length; i++) {
      int index = random.nextInt(charSetSize);
      sb.append(urlSafeChars.charAt(index));
    }

    return sb.toString();
  }

}

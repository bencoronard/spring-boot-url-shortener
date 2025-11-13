package dev.hireben.url_shortener.url.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
final class UrlSafeIdGeneratorTests {

  @Test
  void testGenerateUrlSafeStringWithInvalidArg() {

    Exception exception = assertThrows(IllegalArgumentException.class,
        () -> UrlSafeIdGenerator.generateUrlSafeString(0));

    assertEquals("Length must be positive", exception.getMessage());
  }

  // -----------------------------------------------------------------------------

  @Test
  void testGenerateUrlSafeStringWithValidArg() {
    String actual = UrlSafeIdGenerator.generateUrlSafeString(6);
    String actualEncoded = URLEncoder.encode(actual, StandardCharsets.UTF_8);
    assertEquals(actualEncoded, actual);
  }
}

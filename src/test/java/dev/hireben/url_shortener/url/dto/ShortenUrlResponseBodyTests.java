package dev.hireben.url_shortener.url.dto;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.fasterxml.jackson.databind.ObjectMapper;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
final class ShortenUrlResponseBodyTests {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  // =============================================================================

  @Test
  void serialization_ShouldProduceCorrectJson() throws Exception {
    ShortenUrlResponseBody body = new ShortenUrlResponseBody("https://short.ly/abc123");

    String json = objectMapper.writeValueAsString(body);

    // Verify JSON contains correct property name and value
    Assertions.assertThat(json).contains("\"short_url\":\"https://short.ly/abc123\"");
    Assertions.assertThat(json).doesNotContain("shortUrl");
  }

}

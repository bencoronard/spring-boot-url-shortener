package dev.hireben.url_shortener.url.dto;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

final class ShortenUrlResponseBodyTests {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  // =============================================================================

  @Test
  void serializeObject_shouldCreateCorrectJson() throws Exception {
    ShortenUrlResponseBody body = new ShortenUrlResponseBody("http://localhost/r/abc123");

    String json = objectMapper.writeValueAsString(body);

    // Verify JSON contains correct property name and value
    Assertions.assertThat(json).contains("\"short_url\":\"http://localhost/r/abc123\"");
    Assertions.assertThat(json).doesNotContain("shortUrl");
  }

}

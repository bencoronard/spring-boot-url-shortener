package dev.hireben.url_shortener.url.dto;

import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

final class ShortenUrlRequestBodyTests {

  private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
  private static final ObjectMapper objectMapper = new ObjectMapper();

  // =============================================================================

  @Test
  void validUrl_shouldPassValidation() {
    ShortenUrlRequestBody body = new ShortenUrlRequestBody("https://localhost.com");

    Set<ConstraintViolation<ShortenUrlRequestBody>> violations = validator.validate(body);

    Assertions.assertThat(violations).isEmpty();
  }

  // -----------------------------------------------------------------------------

  @Test
  void blankUrl_shouldFailValidation() {
    ShortenUrlRequestBody body = new ShortenUrlRequestBody(" ");

    Set<ConstraintViolation<ShortenUrlRequestBody>> violations = validator.validate(body);

    // Expect multiple violations: NotBlank and Pattern both might trigger
    Set<String> messages = violations.stream()
        .map(ConstraintViolation::getMessage)
        .collect(Collectors.toSet());

    Assertions.assertThat(messages)
        .contains("URL cannot be blank", "URL must start with http:// or https://");
  }

  // -----------------------------------------------------------------------------

  @Test
  void urlWithoutHttp_shouldFailValidation() {
    ShortenUrlRequestBody body = new ShortenUrlRequestBody("localhost.com");

    Set<ConstraintViolation<ShortenUrlRequestBody>> violations = validator.validate(body);

    Set<String> messages = violations.stream()
        .map(ConstraintViolation::getMessage)
        .collect(Collectors.toSet());

    Assertions.assertThat(messages).containsExactly("URL must start with http:// or https://");
  }

  // -----------------------------------------------------------------------------

  @Test
  void urlTooLong_shouldFailValidation() {
    String longUrl = "https://" + "localhost".repeat(250) + ".com"; // >255 total length
    ShortenUrlRequestBody body = new ShortenUrlRequestBody(longUrl);

    Set<ConstraintViolation<ShortenUrlRequestBody>> violations = validator.validate(body);

    Set<String> messages = violations.stream()
        .map(ConstraintViolation::getMessage)
        .collect(Collectors.toSet());

    Assertions.assertThat(messages).containsExactly("URL length exceeds 255");
  }

  // -----------------------------------------------------------------------------

  @Test
  void deserializeJson_shouldCreateCorrectObject() throws JsonMappingException, JsonProcessingException {
    String json = """
        {
          "original_url": "https://localhost.com"
        }
        """;

    ShortenUrlRequestBody body = objectMapper.readValue(json, ShortenUrlRequestBody.class);

    Assertions.assertThat(body.originalUrl()).isEqualTo("https://localhost.com");
  }

}

package dev.hireben.url_shortener.url.dto;

import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
final class ShortenUrlRequestBodyTests {

  private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
  private static final ObjectMapper objectMapper = new ObjectMapper();

  // =============================================================================
  @Test
  void validUrl_ShouldPassValidation() {
    ShortenUrlRequestBody body = new ShortenUrlRequestBody("https://example.com");

    Set<ConstraintViolation<ShortenUrlRequestBody>> violations = validator.validate(body);

    Assertions.assertThat(violations).isEmpty();
  }

  // -----------------------------------------------------------------------------

  @Test
  void blankUrl_ShouldFailValidation() {
    ShortenUrlRequestBody body = new ShortenUrlRequestBody(" ");

    Set<ConstraintViolation<ShortenUrlRequestBody>> violations = validator.validate(body);

    // Expect multiple violations: NotBlank and Pattern both might trigger
    var messages = violations.stream()
        .map(ConstraintViolation::getMessage)
        .collect(Collectors.toSet());

    Assertions.assertThat(messages)
        .contains("URL cannot be blank", "URL must start with http:// or https://");
  }

  // -----------------------------------------------------------------------------

  @Test
  void urlWithoutHttp_ShouldFailValidation() {
    ShortenUrlRequestBody body = new ShortenUrlRequestBody("www.example.com");

    Set<ConstraintViolation<ShortenUrlRequestBody>> violations = validator.validate(body);

    var messages = violations.stream()
        .map(ConstraintViolation::getMessage)
        .collect(Collectors.toSet());

    Assertions.assertThat(messages).containsExactly("URL must start with http:// or https://");
  }

  // -----------------------------------------------------------------------------

  @Test
  void urlTooLong_ShouldFailValidation() {
    String longUrl = "https://" + "a".repeat(250) + ".com"; // >255 total length
    ShortenUrlRequestBody body = new ShortenUrlRequestBody(longUrl);

    Set<ConstraintViolation<ShortenUrlRequestBody>> violations = validator.validate(body);

    var messages = violations.stream()
        .map(ConstraintViolation::getMessage)
        .collect(Collectors.toSet());

    Assertions.assertThat(messages).containsExactly("URL length exceeds 255");
  }

  // -----------------------------------------------------------------------------

  @Test
  void jsonDeserialization_ShouldMapOriginalUrl() throws Exception {
    String json = """
        {
          "original_url": "https://example.org/path"
        }
        """;

    ShortenUrlRequestBody body = objectMapper.readValue(json, ShortenUrlRequestBody.class);

    Assertions.assertThat(body.originalUrl()).isEqualTo("https://example.org/path");
  }

}

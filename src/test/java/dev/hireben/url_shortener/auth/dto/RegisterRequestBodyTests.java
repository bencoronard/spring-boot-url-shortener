package dev.hireben.url_shortener.auth.dto;

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

final class RegisterRequestBodyTests {

  private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
  private static final ObjectMapper objectMapper = new ObjectMapper();

  // =============================================================================

  @Test
  void validRegisterRequestBody_shouldPassValidation() {
    RegisterRequestBody body = new RegisterRequestBody(
        "user@hireben.dev",
        "P@ssw0rd");

    Set<ConstraintViolation<RegisterRequestBody>> violations = validator.validate(body);

    Assertions.assertThat(violations).isEmpty();
  }

  // -----------------------------------------------------------------------------

  @Test
  void blankEmail_shouldFailValidation() {
    RegisterRequestBody body = new RegisterRequestBody(
        " ",
        "P@ssw0rd");

    Set<ConstraintViolation<RegisterRequestBody>> violations = validator.validate(body);

    Set<String> messages = violations.stream()
        .map(ConstraintViolation::getMessage)
        .collect(Collectors.toSet());

    Assertions.assertThat(messages).contains("Email is required");
  }

  // -----------------------------------------------------------------------------

  @Test
  void invalidEmailFormat_shouldFailValidation() {
    RegisterRequestBody body = new RegisterRequestBody(
        "invalid-email",
        "P@ssw0rd");

    Set<ConstraintViolation<RegisterRequestBody>> violations = validator.validate(body);

    Assertions.assertThat(violations)
        .hasSize(1)
        .first()
        .extracting(ConstraintViolation::getMessage)
        .isEqualTo("Must be a valid email");
  }

  // -----------------------------------------------------------------------------

  @Test
  void blankPassword_shouldFailValidation() {
    RegisterRequestBody body = new RegisterRequestBody(
        "user@hireben.dev",
        " ");

    Set<ConstraintViolation<RegisterRequestBody>> violations = validator.validate(body);

    Set<String> messages = violations.stream()
        .map(ConstraintViolation::getMessage)
        .collect(Collectors.toSet());

    Assertions.assertThat(messages)
        .contains("Password is required",
            "Password must be at least 8 characters long and contain one of each: uppercase, lowercase, digit, and special character.");
  }

  // -----------------------------------------------------------------------------

  @Test
  void invalidPasswordPattern_shouldFailValidation() {
    // Missing uppercase and special character
    RegisterRequestBody body = new RegisterRequestBody(
        "user@hireben.dev",
        "password");

    Set<ConstraintViolation<RegisterRequestBody>> violations = validator.validate(body);

    Assertions.assertThat(violations)
        .hasSize(1)
        .first()
        .extracting(ConstraintViolation::getMessage)
        .isEqualTo(
            "Password must be at least 8 characters long and contain one of each: uppercase, lowercase, digit, and special character.");
  }

  // -----------------------------------------------------------------------------

  @Test
  void deserializeJson_shouldCreateCorrectObject() throws JsonMappingException, JsonProcessingException {
    String json = """
        {
          "email": "user@hireben.dev",
          "password": "P@ssw0rd"
        }
        """;

    RegisterRequestBody body = objectMapper.readValue(json, RegisterRequestBody.class);

    Assertions.assertThat(body.email()).isEqualTo("user@hireben.dev");
    Assertions.assertThat(body.password()).isEqualTo("P@ssw0rd");
  }

}

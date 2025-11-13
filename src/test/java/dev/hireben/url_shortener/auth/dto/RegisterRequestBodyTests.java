package dev.hireben.url_shortener.auth.dto;

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
final class RegisterRequestBodyTests {

  private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
  private static final ObjectMapper objectMapper = new ObjectMapper();

  // =============================================================================

  @Test
  void validRegisterRequestBody_ShouldPassValidation() {
    RegisterRequestBody body = new RegisterRequestBody(
        "user@example.com",
        "StrongP@ssword1");

    Set<ConstraintViolation<RegisterRequestBody>> violations = validator.validate(body);

    Assertions.assertThat(violations).isEmpty();
  }

  // -----------------------------------------------------------------------------

  @Test
  void blankEmail_ShouldFailValidation() {
    RegisterRequestBody body = new RegisterRequestBody(
        " ",
        "StrongP@ssword1");

    Set<ConstraintViolation<RegisterRequestBody>> violations = validator.validate(body);

    Set<String> messages = violations.stream()
        .map(ConstraintViolation::getMessage)
        .collect(Collectors.toSet());

    Assertions.assertThat(messages).contains("Email is required");
  }

  // -----------------------------------------------------------------------------

  @Test
  void invalidEmailFormat_ShouldFailValidation() {
    RegisterRequestBody body = new RegisterRequestBody(
        "invalid-email",
        "StrongP@ssword1");

    Set<ConstraintViolation<RegisterRequestBody>> violations = validator.validate(body);

    Assertions.assertThat(violations)
        .hasSize(1)
        .first()
        .extracting(ConstraintViolation::getMessage)
        .isEqualTo("Must be a valid email");
  }

  // -----------------------------------------------------------------------------

  @Test
  void blankPassword_ShouldFailValidation() {
    RegisterRequestBody body = new RegisterRequestBody(
        "user@example.com",
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
  void invalidPasswordPattern_ShouldFailValidation() {
    // Missing uppercase and special character
    RegisterRequestBody body = new RegisterRequestBody(
        "user@example.com",
        "password1");

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
  void jsonDeserialization_ShouldCreateCorrectObject() throws Exception {
    String json = """
        {
          "email": "user@example.com",
          "password": "StrongP@ssword1"
        }
        """;

    RegisterRequestBody body = objectMapper.readValue(json, RegisterRequestBody.class);

    Assertions.assertThat(body.email()).isEqualTo("user@example.com");
    Assertions.assertThat(body.password()).isEqualTo("StrongP@ssword1");
  }

}

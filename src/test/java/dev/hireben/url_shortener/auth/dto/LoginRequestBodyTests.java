package dev.hireben.url_shortener.auth.dto;

import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
final class LoginRequestBodyTests {

  private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
  private static final ObjectMapper objectMapper = new ObjectMapper();

  // =============================================================================

  @Test
  void validLoginRequestBody_ShouldPassValidation() {
    LoginRequestBody body = new LoginRequestBody("user@example.com", "password123");

    Set<ConstraintViolation<LoginRequestBody>> violations = validator.validate(body);

    Assertions.assertThat(violations).isEmpty();
  }

  // -----------------------------------------------------------------------------

  @Test
  void blankEmail_ShouldFailValidation() {
    LoginRequestBody body = new LoginRequestBody(" ", "password123");

    Set<ConstraintViolation<LoginRequestBody>> violations = validator.validate(body);

    Assertions.assertThat(violations)
        .hasSize(1)
        .first()
        .extracting(ConstraintViolation::getMessage)
        .isEqualTo("Missing email input");
  }

  // -----------------------------------------------------------------------------

  @Test
  void blankPassword_ShouldFailValidation() {
    LoginRequestBody body = new LoginRequestBody("user@example.com", " ");

    Set<ConstraintViolation<LoginRequestBody>> violations = validator.validate(body);

    Assertions.assertThat(violations)
        .hasSize(1)
        .first()
        .extracting(ConstraintViolation::getMessage)
        .isEqualTo("Missing password input");
  }

  // -----------------------------------------------------------------------------

  @Test
  void jsonDeserialization_ShouldCreateCorrectObject() throws Exception {
    String json = """
        {
          "email": "user@example.com",
          "password": "secret"
        }
        """;

    LoginRequestBody body = objectMapper.readValue(json, LoginRequestBody.class);

    Assertions.assertThat(body.email()).isEqualTo("user@example.com");
    Assertions.assertThat(body.password()).isEqualTo("secret");
  }

}

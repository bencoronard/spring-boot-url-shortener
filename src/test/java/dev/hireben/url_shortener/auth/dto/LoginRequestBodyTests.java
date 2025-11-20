package dev.hireben.url_shortener.auth.dto;

import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

final class LoginRequestBodyTests {

  private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
  private static final ObjectMapper objectMapper = new ObjectMapper();

  // =============================================================================

  @Test
  void validLoginRequestBody_shouldPassValidation() {
    LoginRequestBody body = new LoginRequestBody("user@hireben.dev", "P@ssw0rd");

    Set<ConstraintViolation<LoginRequestBody>> violations = validator.validate(body);

    Assertions.assertThat(violations).isEmpty();
  }

  // -----------------------------------------------------------------------------

  @Test
  void blankEmail_shouldFailValidation() {
    LoginRequestBody body = new LoginRequestBody(" ", "P@ssw0rd");

    Set<ConstraintViolation<LoginRequestBody>> violations = validator.validate(body);

    Assertions.assertThat(violations)
        .hasSize(1)
        .first()
        .extracting(ConstraintViolation::getMessage)
        .isEqualTo("Missing email input");
  }

  // -----------------------------------------------------------------------------

  @Test
  void blankPassword_shouldFailValidation() {
    LoginRequestBody body = new LoginRequestBody("user@hireben.dev", " ");

    Set<ConstraintViolation<LoginRequestBody>> violations = validator.validate(body);

    Assertions.assertThat(violations)
        .hasSize(1)
        .first()
        .extracting(ConstraintViolation::getMessage)
        .isEqualTo("Missing password input");
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

    LoginRequestBody body = objectMapper.readValue(json, LoginRequestBody.class);

    Assertions.assertThat(body.email()).isEqualTo("user@hireben.dev");
    Assertions.assertThat(body.password()).isEqualTo("P@ssw0rd");
  }

}

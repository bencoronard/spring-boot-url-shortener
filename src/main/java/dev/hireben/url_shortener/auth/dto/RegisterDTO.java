package dev.hireben.url_shortener.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterDTO(
        @NotBlank(message = "Email is required") @Email(message = "Must be a valid email") String email,
        @NotBlank(message = "Password is required") @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$", message = "Password must be at least 8 characters long and contain one of each: uppercase, lowercase, digit, and special character.") String password) {

}

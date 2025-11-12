package dev.hireben.url_shortener.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginDTO(
    @NotBlank(message = "Missing email input") String email,
    @NotBlank(message = "Missing password input") String password) {

}

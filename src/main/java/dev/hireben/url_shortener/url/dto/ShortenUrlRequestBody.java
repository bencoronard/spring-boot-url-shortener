package dev.hireben.url_shortener.url.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ShortenUrlRequestBody(
		@JsonProperty("original_url") @NotBlank(message = "URL cannot be blank") @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://") @Size(max = 255, message = "URL length exceeds 255") String originalUrl) {

}

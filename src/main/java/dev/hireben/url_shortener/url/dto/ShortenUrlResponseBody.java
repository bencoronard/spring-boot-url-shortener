package dev.hireben.url_shortener.url.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ShortenUrlResponseBody(
		@JsonProperty("short_url") String shortUrl) {

}

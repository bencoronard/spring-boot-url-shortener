package dev.hireben.url_shortener.url.controller;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import dev.hireben.url_shortener.common.annotation.AuthorizationHeader;
import dev.hireben.url_shortener.url.dto.ShortenUrlRequestBody;
import dev.hireben.url_shortener.url.dto.ShortenUrlResponseBody;
import dev.hireben.url_shortener.url.service.UrlMappingService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequiredArgsConstructor
final class UrlMappingController {

  final UrlMappingService urlMappingService;

  // =============================================================================

  @PostMapping("/api/shorten")
  ResponseEntity<ShortenUrlResponseBody> shortenUrl(
      @AuthorizationHeader Claims authClaims,
      @Valid @RequestBody ShortenUrlRequestBody body) {

    String shortUrl = urlMappingService.createUrlMapping(body.originalUrl(), authClaims);

    return ResponseEntity.ok().body(new ShortenUrlResponseBody(shortUrl));
  }

  // -----------------------------------------------------------------------------

  @DeleteMapping("/api/urls/{id}")
  ResponseEntity<Void> deleteShortUrl(
      @AuthorizationHeader Claims authClaims,
      @PathVariable("id") String shortUrlPath) {

    urlMappingService.removeUrlMapping(shortUrlPath, authClaims);

    return ResponseEntity.noContent().build();
  }

  // -----------------------------------------------------------------------------

  @GetMapping("/api/urls")
  ResponseEntity<Slice<String>> listShortUrls(
      @AuthorizationHeader Claims authClaims,
      @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

    Slice<String> shortUrlSlice = urlMappingService.listShortUrls(pageable, authClaims);

    return ResponseEntity.ok().body(shortUrlSlice);
  }

  // -----------------------------------------------------------------------------

  @GetMapping("/r/{id}")
  ResponseEntity<Void> redirectToOriginalUrl(
      @AuthorizationHeader Claims authClaims,
      @PathVariable("id") String shortUrlPath) {

    String originalUrl = urlMappingService.retrieveOriginalUrl(shortUrlPath, authClaims);

    return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(originalUrl)).build();
  }

}

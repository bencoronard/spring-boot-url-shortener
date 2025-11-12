package dev.hireben.url_shortener.url.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import io.jsonwebtoken.Claims;

public interface UrlMappingService {

  String createUrlMapping(String originalUrl, Claims authClaims);

  String retrieveOriginalUrl(String shortUrlPath, Claims authClaims);

  Slice<String> listShortUrls(Pageable pageable, Claims authClaims);

  void removeUrlMapping(String shortUrlPath, Claims authClaims);

}

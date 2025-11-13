package dev.hireben.url_shortener.url.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.hireben.url_shortener.auth.repository.UserRepository;
import dev.hireben.url_shortener.common.exception.InsufficientPermissionException;
import dev.hireben.url_shortener.url.entity.UrlMapping;
import dev.hireben.url_shortener.url.exception.UrlMappingNotFoundException;
import dev.hireben.url_shortener.url.exception.UrlShortenExceedMaxAttemptsException;
import dev.hireben.url_shortener.url.repository.UrlMappingRepository;
import dev.hireben.url_shortener.url.utility.UrlSafeIdGenerator;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class UrlMappingServiceImpl implements UrlMappingService {

  final UrlMappingRepository urlMappingRepository;
  final UserRepository userRepository;

  static final String shortUrlFormat = "%s/r/%s";

  @Value("${internal.host.external-url}")
  String hostExternalUrl;
  @Value("${internal.url.id-gen-max-attempts}")
  Integer idGenMaxAttempt;

  // =============================================================================

  @Override
  @Transactional
  public String createUrlMapping(String originalUrl, Claims authClaims) {

    Integer permissionValue = authClaims.get("CREATE_URL_MAPPING", Integer.class);
    if (permissionValue == null) {
      throw new InsufficientPermissionException("Not allowed to create short URLs");
    }

    Long userId = Long.parseLong(authClaims.getSubject());

    String existingShortUrlPath = urlMappingRepository.findShortUrlPathByOriginalUrlAndCreatedById(originalUrl, userId);
    if (existingShortUrlPath != null) {
      return String.format(shortUrlFormat, hostExternalUrl, existingShortUrlPath);
    }

    for (int i = 0; i < idGenMaxAttempt; i++) {
      String newShortUrlPath = UrlSafeIdGenerator.generateUrlSafeString(6);

      if (!urlMappingRepository.existsById(newShortUrlPath)) {

        UrlMapping newUrlMapping = UrlMapping.builder()
            .shortUrlPath(newShortUrlPath)
            .originalUrl(originalUrl)
            .createdBy(userRepository.getReferenceById(userId))
            .createdAt(Instant.now())
            .build();

        urlMappingRepository.save(newUrlMapping);

        return String.format(shortUrlFormat, hostExternalUrl, newShortUrlPath);
      }
    }

    throw new UrlShortenExceedMaxAttemptsException("URL shortening failed. Try again.");
  }

  // -----------------------------------------------------------------------------

  @Override
  @Transactional(readOnly = true)
  public String retrieveOriginalUrl(String shortUrlPath, Claims authClaims) {

    String originalUrl = urlMappingRepository.findOriginalUrlByShortUrlPathAndCreatedById(shortUrlPath,
        Long.parseLong(authClaims.getSubject()));

    if (originalUrl == null) {
      throw new UrlMappingNotFoundException(
          String.format("No URL mapping found for " + shortUrlFormat, hostExternalUrl, shortUrlPath));
    }

    return originalUrl;
  }

  // -----------------------------------------------------------------------------

  @Override
  @Transactional(readOnly = true)
  public Slice<String> listShortUrls(Pageable pageable, Claims authClaims) {

    Integer permissionValue = authClaims.get("LIST_URL_MAPPING", Integer.class);
    if (permissionValue == null) {
      throw new InsufficientPermissionException("Not allowed to list short URLs");
    }

    return urlMappingRepository.findAllShortUrlPathByCreatedById(pageable, Long.parseLong(authClaims.getSubject()));
  }

  // -----------------------------------------------------------------------------

  @Override
  @Transactional
  public void removeUrlMapping(String shortUrlPath, Claims authClaims) {

    Integer permissionValue = authClaims.get("DELETE_URL_MAPPING", Integer.class);
    if (permissionValue == null) {
      throw new InsufficientPermissionException("Not allowed to delete short URLs");
    }

    urlMappingRepository.deleteByShortUrlPathAndCreatedById(shortUrlPath, Long.parseLong(authClaims.getSubject()));
  }

}

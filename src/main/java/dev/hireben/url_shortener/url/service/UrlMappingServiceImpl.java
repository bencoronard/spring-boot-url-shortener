package dev.hireben.url_shortener.url.service;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.hireben.url_shortener.auth.repository.UserRepository;
import dev.hireben.url_shortener.common.exception.InsufficientPermissionException;
import dev.hireben.url_shortener.url.entity.UrlMapping;
import dev.hireben.url_shortener.url.exception.UrlMappingNotFoundException;
import dev.hireben.url_shortener.url.exception.UrlShortenExceedMaxAttemptsException;
import dev.hireben.url_shortener.url.repository.UrlMappingRepository;
import dev.hireben.url_shortener.url.utility.UrlPermission;
import dev.hireben.url_shortener.url.utility.UrlSafeIdGenerator;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class UrlMappingServiceImpl implements UrlMappingService {

  private final UrlMappingRepository urlMappingRepository;
  private final UserRepository userRepository;

  private static final String SHORT_URL_FORMAT = "%s/r/%s";

  @Value("${internal.host.external-url}")
  private String hostExternalUrl;
  @Value("${internal.url.id-gen-max-attempts}")
  private Integer idGenMaxAttempt;

  // =============================================================================

  @Override
  @Transactional
  public String createUrlMapping(String originalUrl, Claims authClaims) {

    Integer permissionValue = authClaims.get(UrlPermission.CREATE_URL_MAPPING, Integer.class);
    if (permissionValue == null) {
      throw new InsufficientPermissionException("Not allowed to create short URLs");
    }

    Long userId = Long.parseLong(authClaims.getSubject());

    UrlMapping existingUrlMapping = urlMappingRepository.findByOriginalUrlAndCreatedById(originalUrl, userId);
    if (existingUrlMapping != null) {
      return String.format(SHORT_URL_FORMAT, hostExternalUrl, existingUrlMapping.getShortUrlPath());
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

        return String.format(SHORT_URL_FORMAT, hostExternalUrl, newShortUrlPath);
      }
    }

    throw new UrlShortenExceedMaxAttemptsException("URL shortening failed. Try again.");
  }

  // -----------------------------------------------------------------------------

  @Override
  @Transactional(readOnly = true)
  public String retrieveOriginalUrl(String shortUrlPath, Claims authClaims) {

    UrlMapping existingUrlMapping = urlMappingRepository.findByShortUrlPathAndCreatedById(shortUrlPath,
        Long.parseLong(authClaims.getSubject()));

    if (existingUrlMapping == null) {
      throw new UrlMappingNotFoundException(
          String.format("No URL mapping found for " + SHORT_URL_FORMAT, hostExternalUrl, shortUrlPath));
    }

    return existingUrlMapping.getOriginalUrl();
  }

  // -----------------------------------------------------------------------------

  @Override
  @Transactional(readOnly = true)
  public Slice<String> listShortUrls(Pageable pageable, Claims authClaims) {

    Integer permissionValue = authClaims.get(UrlPermission.LIST_URL_MAPPING, Integer.class);
    if (permissionValue == null) {
      throw new InsufficientPermissionException("Not allowed to list short URLs");
    }

    Slice<UrlMapping> urlMappingSlice = urlMappingRepository.findAllByCreatedById(pageable,
        Long.parseLong(authClaims.getSubject()));

    return new SliceImpl<>(
        urlMappingSlice.getContent().stream().map(
            mapping -> String.format(SHORT_URL_FORMAT, hostExternalUrl, mapping.getShortUrlPath())).toList(),
        urlMappingSlice.getPageable(),
        urlMappingSlice.hasNext());
  }

  // -----------------------------------------------------------------------------

  @Override
  @Transactional
  public void removeUrlMapping(String shortUrlPath, Claims authClaims) {

    Integer permissionValue = authClaims.get(UrlPermission.DELETE_URL_MAPPING, Integer.class);
    if (permissionValue == null) {
      throw new InsufficientPermissionException("Not allowed to delete short URLs");
    }

    urlMappingRepository.deleteByShortUrlPathAndCreatedById(shortUrlPath, Long.parseLong(authClaims.getSubject()));
  }

}

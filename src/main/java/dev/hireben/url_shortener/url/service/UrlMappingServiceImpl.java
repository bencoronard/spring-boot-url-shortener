package dev.hireben.url_shortener.url.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import dev.hireben.url_shortener.url.repository.UrlMappingRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
final class UrlMappingServiceImpl implements UrlMappingService {

  final UrlMappingRepository urlMappingRepository;

  @Value("${internal.host.external-url}")
  String hostExternalUrl;

  // =============================================================================

  @Override
  public String createUrlMapping(String originalUrl, Claims authClaims) {
    return String.format("%s/r/%s", hostExternalUrl, "123456");
  }

  // -----------------------------------------------------------------------------

  @Override
  public String retrieveOriginalUrl(String shortUrlPath, Claims authClaims) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'retrieveOriginalUrl'");
  }

  // -----------------------------------------------------------------------------

  @Override
  public Slice<String> listShortUrls(Pageable pageable, Claims authClaims) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'listShortUrls'");
  }

  // -----------------------------------------------------------------------------

  @Override
  public void removeUrlMapping(String shortUrlPath, Claims authClaims) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'removeUrlMapping'");
  }

}

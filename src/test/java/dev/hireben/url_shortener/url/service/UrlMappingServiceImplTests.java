package dev.hireben.url_shortener.url.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import dev.hireben.url_shortener.auth.entity.User;
import dev.hireben.url_shortener.auth.repository.UserRepository;
import dev.hireben.url_shortener.common.exception.InsufficientPermissionException;
import dev.hireben.url_shortener.url.entity.UrlMapping;
import dev.hireben.url_shortener.url.exception.UrlMappingNotFoundException;
import dev.hireben.url_shortener.url.exception.UrlShortenExceedMaxAttemptsException;
import dev.hireben.url_shortener.url.repository.UrlMappingRepository;
import dev.hireben.url_shortener.url.utility.UrlSafeIdGenerator;
import io.jsonwebtoken.Claims;

@ExtendWith(MockitoExtension.class)
final class UrlMappingServiceImplTests {

  @Mock
  private UrlMappingRepository urlMappingRepository;
  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UrlMappingServiceImpl urlMappingService;

  // =============================================================================

  @BeforeEach
  void setup() {
    ReflectionTestUtils.setField(urlMappingService, "hostExternalUrl", "http://localhost");
    ReflectionTestUtils.setField(urlMappingService, "idGenMaxAttempt", 3);
  }

  // =============================================================================

  @Test
  void createUrlMappingWithNoPermission_ShouldThrowException() {
    Claims claims = mock(Claims.class);
    when(claims.get("CREATE_URL_MAPPING", Integer.class)).thenReturn(null);

    assertThrows(InsufficientPermissionException.class, () -> urlMappingService.createUrlMapping("url", claims));
  }

  // -----------------------------------------------------------------------------

  @Test
  void createUrlMapping_ShouldReturnExistingShortUrl_whenOriginalUrlExists() {
    Claims claims = mock(Claims.class);
    when(claims.get("CREATE_URL_MAPPING", Integer.class)).thenReturn(1);
    when(claims.getSubject()).thenReturn("1");

    UrlMapping exising = UrlMapping.builder()
        .shortUrlPath("abc123")
        .build();

    when(urlMappingRepository.findByOriginalUrlAndCreatedById("http://localhost.com", 1L)).thenReturn(exising);

    String result = urlMappingService.createUrlMapping("http://localhost.com", claims);

    assertEquals("http://localhost/r/abc123", result);
  }

  // -----------------------------------------------------------------------------

  @Test
  void createUrlMapping_ShouldGenerateShortUrl() {
    Claims claims = mock(Claims.class);
    when(claims.get("CREATE_URL_MAPPING", Integer.class)).thenReturn(1);
    when(claims.getSubject()).thenReturn("1");
    when(urlMappingRepository.findByOriginalUrlAndCreatedById("http://localhost.com", 1L)).thenReturn(null);

    try (MockedStatic<UrlSafeIdGenerator> urlSafeIdGenerator = mockStatic(UrlSafeIdGenerator.class)) {
      urlSafeIdGenerator.when(() -> UrlSafeIdGenerator.generateUrlSafeString(6)).thenReturn("abc123");

      when(urlMappingRepository.existsById("abc123")).thenReturn(false);
      when(userRepository.getReferenceById(1L)).thenReturn(User.builder().id(1L).build());

      String result = urlMappingService.createUrlMapping("http://localhost.com", claims);

      assertEquals("http://localhost/r/abc123", result);
      verify(urlMappingRepository).save(any(UrlMapping.class));
    }
  }

  // -----------------------------------------------------------------------------

  @Test
  void createUrlMapping_ShouldFail_afterMaxAttempts() {
    Claims claims = mock(Claims.class);
    when(claims.get("CREATE_URL_MAPPING", Integer.class)).thenReturn(1);
    when(claims.getSubject()).thenReturn("1");
    when(urlMappingRepository.findByOriginalUrlAndCreatedById("http://localhost.com", 1L)).thenReturn(null);

    try (MockedStatic<UrlSafeIdGenerator> urlSafeIdGenerator = mockStatic(UrlSafeIdGenerator.class)) {
      urlSafeIdGenerator.when(() -> UrlSafeIdGenerator.generateUrlSafeString(6))
          .thenReturn("abc111", "abc222", "abc333");

      when(urlMappingRepository.existsById(any())).thenReturn(true);

      assertThrows(UrlShortenExceedMaxAttemptsException.class,
          () -> urlMappingService.createUrlMapping("http://localhost.com", claims));
    }
  }

  // -----------------------------------------------------------------------------

  @Test
  void retrieveOriginalUrl_ShouldThrowException_whenNotFound() {
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("1");
    when(urlMappingRepository.findByShortUrlPathAndCreatedById("abc123", 1L)).thenReturn(null);

    assertThrows(UrlMappingNotFoundException.class, () -> urlMappingService.retrieveOriginalUrl("abc123", claims));
  }

  // -----------------------------------------------------------------------------

  @Test
  void retrieveOriginalUrl_ShouldReturnOriginalUrl() {
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("1");

    UrlMapping urlMapping = UrlMapping.builder()
        .originalUrl("http://localhost.com")
        .build();

    when(urlMappingRepository.findByShortUrlPathAndCreatedById("abc123", 1L)).thenReturn(urlMapping);

    String result = urlMappingService.retrieveOriginalUrl("abc123", claims);

    assertEquals("http://localhost.com", result);
  }

  // -----------------------------------------------------------------------------

  @Test
  void listShortUrls_ShouldThrowException_whenNoPermission() {
    Claims claims = mock(Claims.class);
    when(claims.get("LIST_URL_MAPPING", Integer.class)).thenReturn(null);

    assertThrows(InsufficientPermissionException.class,
        () -> urlMappingService.listShortUrls(PageRequest.of(0, 10), claims));
  }

  // -----------------------------------------------------------------------------

  @Test
  void listShortUrls_ShouldReturnShortUrlSlice() {
    Claims claims = mock(Claims.class);
    when(claims.get("LIST_URL_MAPPING", Integer.class)).thenReturn(1);
    when(claims.getSubject()).thenReturn("1");

    List<UrlMapping> list = List.of(
        UrlMapping.builder().shortUrlPath("abc111").build(),
        UrlMapping.builder().shortUrlPath("abc222").build());

    Slice<UrlMapping> slice = new SliceImpl<>(list);

    when(urlMappingRepository.findAllByCreatedById(any(), eq(1L))).thenReturn(slice);

    Slice<String> result = urlMappingService.listShortUrls(PageRequest.of(0, 10), claims);

    assertEquals(List.of("http://localhost/r/abc111", "http://localhost/r/abc222"), result.getContent());
  }

  // -----------------------------------------------------------------------------

  @Test
  void removeUrlMapping_ShouldThrowException_whenNoPermission() {
    Claims claims = mock(Claims.class);
    when(claims.get("DELETE_URL_MAPPING", Integer.class)).thenReturn(null);

    assertThrows(InsufficientPermissionException.class,
        () -> urlMappingService.removeUrlMapping("abc123", claims));
  }

  // -----------------------------------------------------------------------------

  @Test
  void removeUrlMapping_ShouldCallRepository() {
    Claims claims = mock(Claims.class);
    when(claims.get("DELETE_URL_MAPPING", Integer.class)).thenReturn(1);
    when(claims.getSubject()).thenReturn("1");

    urlMappingService.removeUrlMapping("abc123", claims);

    verify(urlMappingRepository).deleteByShortUrlPathAndCreatedById("abc123", 1L);
  }

}

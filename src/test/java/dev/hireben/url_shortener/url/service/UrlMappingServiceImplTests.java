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
import dev.hireben.url_shortener.url.utility.UrlPermission;
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

  // =============================================================================\

  private static final String hostExternalUrl = "http://localhost:8080";
  private static final String mockOriginalUrl = "https://localhost.svc.cluster.local";
  private static final String redirectBaseUrl = hostExternalUrl + "/r/";

  // =============================================================================

  @BeforeEach
  void setup() {
    ReflectionTestUtils.setField(urlMappingService, "hostExternalUrl", hostExternalUrl);
    ReflectionTestUtils.setField(urlMappingService, "idGenMaxAttempt", 3);
  }

  // =============================================================================

  @Test
  void createUrlMapping_withoutPermission_shouldThrowException() {
    Claims claims = mock(Claims.class);
    when(claims.get(UrlPermission.CREATE_URL_MAPPING, Integer.class)).thenReturn(null);

    assertThrows(InsufficientPermissionException.class,
        () -> urlMappingService.createUrlMapping(mockOriginalUrl, claims));
  }

  // -----------------------------------------------------------------------------

  @Test
  void createUrlMapping_whenOriginalUrlExists_shouldReturnExistingShortUrl() {
    Claims claims = mock(Claims.class);
    when(claims.get(UrlPermission.CREATE_URL_MAPPING, Integer.class)).thenReturn(1);
    when(claims.getSubject()).thenReturn("1");

    String path = "abc123";

    UrlMapping exising = UrlMapping.builder()
        .shortUrlPath(path)
        .build();

    when(urlMappingRepository.findByOriginalUrlAndCreatedById(mockOriginalUrl, 1L)).thenReturn(exising);

    String result = urlMappingService.createUrlMapping(mockOriginalUrl, claims);

    assertEquals(redirectBaseUrl + path, result);
  }

  // -----------------------------------------------------------------------------

  @Test
  void createUrlMapping_whenOriginalUrlDoesNotExist_shouldCreateNewShortUrl() {
    Claims claims = mock(Claims.class);
    when(claims.get(UrlPermission.CREATE_URL_MAPPING, Integer.class)).thenReturn(1);
    when(claims.getSubject()).thenReturn("1");
    when(urlMappingRepository.findByOriginalUrlAndCreatedById(mockOriginalUrl, 1L)).thenReturn(null);

    try (MockedStatic<UrlSafeIdGenerator> urlSafeIdGenerator = mockStatic(UrlSafeIdGenerator.class)) {

      String path = "abc123";

      urlSafeIdGenerator.when(() -> UrlSafeIdGenerator.generateUrlSafeString(6)).thenReturn(path);

      when(urlMappingRepository.existsById(path)).thenReturn(false);
      when(userRepository.getReferenceById(1L)).thenReturn(User.builder().build());

      String result = urlMappingService.createUrlMapping(mockOriginalUrl, claims);

      assertEquals(redirectBaseUrl + path, result);
      verify(urlMappingRepository).save(any(UrlMapping.class));
    }
  }

  // -----------------------------------------------------------------------------

  @Test
  void createUrlMapping_afterMaxFailedAttempts_shouldThrowException() {
    Claims claims = mock(Claims.class);
    when(claims.get(UrlPermission.CREATE_URL_MAPPING, Integer.class)).thenReturn(1);
    when(claims.getSubject()).thenReturn("1");
    when(urlMappingRepository.findByOriginalUrlAndCreatedById(mockOriginalUrl, 1L)).thenReturn(null);

    try (MockedStatic<UrlSafeIdGenerator> urlSafeIdGenerator = mockStatic(UrlSafeIdGenerator.class)) {
      urlSafeIdGenerator.when(() -> UrlSafeIdGenerator.generateUrlSafeString(6))
          .thenReturn("aaa111", "bbb222", "ccc333");

      when(urlMappingRepository.existsById(any())).thenReturn(true);

      assertThrows(UrlShortenExceedMaxAttemptsException.class,
          () -> urlMappingService.createUrlMapping(mockOriginalUrl, claims));
    }
  }

  // -----------------------------------------------------------------------------

  @Test
  void retrieveOriginalUrl_whenNotFound_shouldThrowException() {
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("1");

    String path = "abc123";

    when(urlMappingRepository.findByShortUrlPathAndCreatedById(path, 1L)).thenReturn(null);

    assertThrows(UrlMappingNotFoundException.class, () -> urlMappingService.retrieveOriginalUrl(path, claims));
  }

  // -----------------------------------------------------------------------------

  @Test
  void retrieveOriginalUrl_shouldReturnOriginalUrl() {
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("1");

    UrlMapping urlMapping = UrlMapping.builder()
        .originalUrl(mockOriginalUrl)
        .build();

    String path = "abc123";

    when(urlMappingRepository.findByShortUrlPathAndCreatedById(path, 1L)).thenReturn(urlMapping);

    String result = urlMappingService.retrieveOriginalUrl(path, claims);

    assertEquals(mockOriginalUrl, result);
  }

  // -----------------------------------------------------------------------------

  @Test
  void listShortUrls_withoutPermission_shouldThrowException() {
    Claims claims = mock(Claims.class);
    when(claims.get(UrlPermission.LIST_URL_MAPPING, Integer.class)).thenReturn(null);

    assertThrows(InsufficientPermissionException.class,
        () -> urlMappingService.listShortUrls(PageRequest.of(0, 10), claims));
  }

  // -----------------------------------------------------------------------------

  @Test
  void listShortUrls_shouldReturnShortUrlSlice() {
    Claims claims = mock(Claims.class);
    when(claims.get(UrlPermission.LIST_URL_MAPPING, Integer.class)).thenReturn(1);
    when(claims.getSubject()).thenReturn("1");

    String path1 = "aaa111";
    String path2 = "bbb222";

    List<UrlMapping> list = List.of(
        UrlMapping.builder().shortUrlPath(path1).build(),
        UrlMapping.builder().shortUrlPath(path2).build());

    Slice<UrlMapping> slice = new SliceImpl<>(list);

    when(urlMappingRepository.findAllByCreatedById(any(), eq(1L))).thenReturn(slice);

    Slice<String> result = urlMappingService.listShortUrls(PageRequest.of(0, 10), claims);

    assertEquals(List.of(redirectBaseUrl + path1, redirectBaseUrl + path2), result.getContent());
  }

  // -----------------------------------------------------------------------------

  @Test
  void removeUrlMapping_withoutPermission_shouldThrowException() {
    Claims claims = mock(Claims.class);
    when(claims.get(UrlPermission.DELETE_URL_MAPPING, Integer.class)).thenReturn(null);

    assertThrows(InsufficientPermissionException.class,
        () -> urlMappingService.removeUrlMapping("abc123", claims));
  }

  // -----------------------------------------------------------------------------

  @Test
  void removeUrlMapping_shouldCallRepositoryDelete() {
    Claims claims = mock(Claims.class);
    when(claims.get(UrlPermission.DELETE_URL_MAPPING, Integer.class)).thenReturn(1);
    when(claims.getSubject()).thenReturn("1");

    String path = "abc123";

    urlMappingService.removeUrlMapping(path, claims);

    verify(urlMappingRepository).deleteByShortUrlPathAndCreatedById(path, 1L);
  }

}

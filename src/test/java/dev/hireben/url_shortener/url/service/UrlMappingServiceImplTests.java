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
  private static final String redirectBaseUrl = hostExternalUrl + "/r/";
  private static final String mockOriginalUrl = "https://localhost.svc.cluster.local";
  private static final String mockShortUrlPath1 = "aaa111";
  private static final String mockShortUrlPath2 = "bbb222";
  private static final Long mockUserId = 1L;
  private static final String mockUserIdString = "1";

  // =============================================================================

  @BeforeEach
  void setup() {
    ReflectionTestUtils.setField(urlMappingService, "hostExternalUrl", hostExternalUrl);
    ReflectionTestUtils.setField(urlMappingService, "idGenMaxAttempt", 2);
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
    when(claims.getSubject()).thenReturn(mockUserIdString);

    UrlMapping exising = UrlMapping.builder()
        .shortUrlPath(mockShortUrlPath1)
        .build();

    when(urlMappingRepository.findByOriginalUrlAndCreatedById(mockOriginalUrl, mockUserId)).thenReturn(exising);

    String result = urlMappingService.createUrlMapping(mockOriginalUrl, claims);

    assertEquals(redirectBaseUrl + mockShortUrlPath1, result);
  }

  // -----------------------------------------------------------------------------

  @Test
  void createUrlMapping_whenOriginalUrlDoesNotExist_shouldCreateNewShortUrl() {
    Claims claims = mock(Claims.class);
    when(claims.get(UrlPermission.CREATE_URL_MAPPING, Integer.class)).thenReturn(1);
    when(claims.getSubject()).thenReturn(mockUserIdString);
    when(urlMappingRepository.findByOriginalUrlAndCreatedById(mockOriginalUrl, mockUserId)).thenReturn(null);

    try (MockedStatic<UrlSafeIdGenerator> urlSafeIdGenerator = mockStatic(UrlSafeIdGenerator.class)) {

      urlSafeIdGenerator.when(() -> UrlSafeIdGenerator.generateUrlSafeString(6)).thenReturn(mockShortUrlPath1);

      when(urlMappingRepository.existsById(mockShortUrlPath1)).thenReturn(false);
      when(userRepository.getReferenceById(mockUserId)).thenReturn(User.builder().build());

      String result = urlMappingService.createUrlMapping(mockOriginalUrl, claims);

      assertEquals(redirectBaseUrl + mockShortUrlPath1, result);
      verify(urlMappingRepository).save(any(UrlMapping.class));
    }
  }

  // -----------------------------------------------------------------------------

  @Test
  void createUrlMapping_afterMaxFailedAttempts_shouldThrowException() {
    Claims claims = mock(Claims.class);
    when(claims.get(UrlPermission.CREATE_URL_MAPPING, Integer.class)).thenReturn(1);
    when(claims.getSubject()).thenReturn(mockUserIdString);
    when(urlMappingRepository.findByOriginalUrlAndCreatedById(mockOriginalUrl, mockUserId)).thenReturn(null);

    try (MockedStatic<UrlSafeIdGenerator> urlSafeIdGenerator = mockStatic(UrlSafeIdGenerator.class)) {
      urlSafeIdGenerator.when(() -> UrlSafeIdGenerator.generateUrlSafeString(6))
          .thenReturn(mockShortUrlPath1, mockShortUrlPath2);

      when(urlMappingRepository.existsById(any())).thenReturn(true);

      assertThrows(UrlShortenExceedMaxAttemptsException.class,
          () -> urlMappingService.createUrlMapping(mockOriginalUrl, claims));
    }
  }

  // -----------------------------------------------------------------------------

  @Test
  void retrieveOriginalUrl_whenNotFound_shouldThrowException() {
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn(mockUserIdString);

    when(urlMappingRepository.findByShortUrlPathAndCreatedById(mockShortUrlPath1, mockUserId)).thenReturn(null);

    assertThrows(UrlMappingNotFoundException.class,
        () -> urlMappingService.retrieveOriginalUrl(mockShortUrlPath1, claims));
  }

  // -----------------------------------------------------------------------------

  @Test
  void retrieveOriginalUrl_shouldReturnOriginalUrl() {
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn(mockUserIdString);

    UrlMapping urlMapping = UrlMapping.builder()
        .originalUrl(mockOriginalUrl)
        .build();

    when(urlMappingRepository.findByShortUrlPathAndCreatedById(mockShortUrlPath1, mockUserId)).thenReturn(urlMapping);

    String result = urlMappingService.retrieveOriginalUrl(mockShortUrlPath1, claims);

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
    when(claims.getSubject()).thenReturn(mockUserIdString);

    List<UrlMapping> list = List.of(
        UrlMapping.builder().shortUrlPath(mockShortUrlPath1).build(),
        UrlMapping.builder().shortUrlPath(mockShortUrlPath2).build());

    Slice<UrlMapping> slice = new SliceImpl<>(list);

    when(urlMappingRepository.findAllByCreatedById(any(), eq(mockUserId))).thenReturn(slice);

    Slice<String> result = urlMappingService.listShortUrls(PageRequest.of(0, 10), claims);

    assertEquals(List.of(redirectBaseUrl + mockShortUrlPath1, redirectBaseUrl + mockShortUrlPath2),
        result.getContent());
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
    when(claims.getSubject()).thenReturn(mockUserIdString);

    urlMappingService.removeUrlMapping(mockShortUrlPath1, claims);

    verify(urlMappingRepository).deleteByShortUrlPathAndCreatedById(mockShortUrlPath1, mockUserId);
  }

}

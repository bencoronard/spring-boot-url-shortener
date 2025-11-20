package dev.hireben.url_shortener.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import dev.hireben.url_shortener.auth.entity.Permission;
import dev.hireben.url_shortener.auth.entity.User;
import dev.hireben.url_shortener.auth.exception.InvalidCredentialsException;
import dev.hireben.url_shortener.auth.exception.UserAlreadyExistsException;
import dev.hireben.url_shortener.auth.repository.PermissionRepository;
import dev.hireben.url_shortener.auth.repository.UserRepository;
import dev.hireben.url_shortener.common.utility.jwt.api.JwtIssuer;

@ExtendWith(MockitoExtension.class)
final class AuthServiceImplTests {

  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private JwtIssuer jwtIssuer;
  @Mock
  private UserRepository userRepository;
  @Mock
  private PermissionRepository permissionRepository;

  @InjectMocks
  private AuthServiceImpl authService;

  // =============================================================================

  private static final String dummyHash = "dummyH@sh";
  private static final String mockValidEmail = "user@hireben.dev";
  private static final String mockValidEmailInCaps = "USER@hireben.dev";
  private static final String mockValidPassword = "P@ssw0rd";
  private static final String mockPasswordHash = "P@ssw0rdH@sh";
  private static final String mockToken = "t0ken";

  // =============================================================================

  @BeforeEach
  void setup() {
    ReflectionTestUtils.setField(authService, "tokenTtlInSec", 30);
    ReflectionTestUtils.setField(authService, "dummyPasswordHash", dummyHash);
  }

  // =============================================================================

  @Test
  void register_withExistingEmail_shouldThrowException() {
    when(userRepository.existsByEmail(mockValidEmail)).thenReturn(true);

    assertThrows(UserAlreadyExistsException.class, () -> authService.register(mockValidEmailInCaps, mockValidPassword));
  }

  // -----------------------------------------------------------------------------

  @Test
  void register_withNewEmail_shouldCreateNewUser() {
    when(userRepository.existsByEmail(mockValidEmail)).thenReturn(false);
    when(passwordEncoder.encode(mockValidPassword)).thenReturn(mockPasswordHash);

    authService.register(mockValidEmailInCaps, mockValidPassword);

    verify(userRepository).save(
        argThat(user -> user.getEmail().equals(mockValidEmail) && user.getPassword().equals(mockPasswordHash)));
  }

  // -----------------------------------------------------------------------------

  @Test
  void login_withNonExistentEmail_shouldThrowException() {
    when(userRepository.findByEmail(mockValidEmail)).thenReturn(null);
    when(passwordEncoder.matches(mockValidPassword, dummyHash)).thenReturn(false);

    assertThrows(InvalidCredentialsException.class, () -> authService.login(mockValidEmailInCaps, mockValidPassword));
  }

  // -----------------------------------------------------------------------------

  @Test
  void login_withIncorrectPassword_shouldThrowException() {
    User user = User.builder().email(mockValidEmail).password(mockPasswordHash).build();

    when(userRepository.findByEmail(mockValidEmail)).thenReturn(user);
    when(passwordEncoder.matches(mockValidPassword, mockPasswordHash)).thenReturn(false);

    assertThrows(InvalidCredentialsException.class, () -> authService.login(mockValidEmailInCaps, mockValidPassword));
  }

  // -----------------------------------------------------------------------------

  @Test
  void login_withValidCredentials_shouldReturnToken() {
    User user = User.builder()
        .id(1L)
        .email(mockValidEmail)
        .password(mockPasswordHash)
        .build();

    when(userRepository.findByEmail(mockValidEmail)).thenReturn(user);
    when(passwordEncoder.matches(mockValidPassword, mockPasswordHash)).thenReturn(true);

    Permission p1 = Permission.builder().permission("ACTION_1").build();
    Permission p2 = Permission.builder().permission("ACTION_2").build();

    when(permissionRepository.findAll()).thenReturn(List.of(p1, p2));
    when(jwtIssuer.issueToken(eq("1"), isNull(), anyMap(), eq(Duration.ofSeconds(30L)), isNull()))
        .thenReturn(mockToken);

    String token = authService.login(mockValidEmailInCaps, mockValidPassword);

    assertEquals(mockToken, token);
  }

  // -----------------------------------------------------------------------------

  @Test
  void login_withNonExistentEmail_shouldPerformHashingAndThrowException() {
    when(userRepository.findByEmail(mockValidEmail)).thenReturn(null);
    when(passwordEncoder.matches(mockValidPassword, dummyHash)).thenReturn(false);

    assertThrows(InvalidCredentialsException.class, () -> authService.login(mockValidEmailInCaps, mockValidPassword));

    verify(passwordEncoder).matches(mockValidPassword, dummyHash);
  }

}

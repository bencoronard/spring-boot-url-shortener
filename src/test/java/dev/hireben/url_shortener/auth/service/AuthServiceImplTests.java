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

  @BeforeEach
  void setup() {
    ReflectionTestUtils.setField(authService, "tokenTtlInSec", 30);
    ReflectionTestUtils.setField(authService, "dummyPasswordHash", "dummyH@sh");
  }

  // =============================================================================

  @Test
  void registerWithExistingEmail_ShouldThrowException() {
    when(userRepository.existsByEmail("user@hireben.dev")).thenReturn(true);

    assertThrows(UserAlreadyExistsException.class, () -> authService.register("USER@hireben.dev", "P@ssw0rd"));
  }

  // -----------------------------------------------------------------------------

  @Test
  void registerWithNewEmail_ShouldSaveUser() {
    when(userRepository.existsByEmail("user@hireben.dev")).thenReturn(false);
    when(passwordEncoder.encode("P@ssw0rd")).thenReturn("H@shedP@ssw0rd");

    authService.register("USER@hireben.dev", "P@ssw0rd");

    verify(userRepository).save(
        argThat(user -> user.getEmail().equals("user@hireben.dev") && user.getPassword().equals("H@shedP@ssw0rd")));
  }

  // -----------------------------------------------------------------------------

  @Test
  void loginWithNonExistentEmail_ShouldThrowException() {
    when(userRepository.findByEmail("user@hireben.dev")).thenReturn(null);
    when(passwordEncoder.matches("P@ssw0rd", "dummyH@sh")).thenReturn(false);

    assertThrows(InvalidCredentialsException.class, () -> authService.login("USER@hireben.dev", "P@ssw0rd"));
  }

  // -----------------------------------------------------------------------------

  @Test
  void loginWithIncorrectPassword_ShouldThrowException() {
    User user = User.builder().email("user@hireben.dev").password("H@shedP@ssw0rd").build();

    when(userRepository.findByEmail("user@hireben.dev")).thenReturn(user);
    when(passwordEncoder.matches("P@ssw0rd", "H@shedP@ssw0rd")).thenReturn(false);

    assertThrows(InvalidCredentialsException.class, () -> authService.login("USER@hireben.dev", "P@ssw0rd"));
  }

  // -----------------------------------------------------------------------------

  @Test
  void loginWithValidCredentials_ShouldReturnToken() {
    User user = User.builder()
        .id(1L)
        .email("user@hireben.dev")
        .password("H@shedP@ssw0rd")
        .build();

    when(userRepository.findByEmail("user@hireben.dev")).thenReturn(user);
    when(passwordEncoder.matches("P@ssw0rd", "H@shedP@ssw0rd")).thenReturn(true);

    Permission p1 = Permission.builder().permission("ACTION_1").build();
    Permission p2 = Permission.builder().permission("ACTION_2").build();

    when(permissionRepository.findAll()).thenReturn(List.of(p1, p2));
    when(jwtIssuer.issueToken(eq("1"), isNull(), anyMap(), eq(Duration.ofSeconds(30L)), isNull())).thenReturn("jwt");

    String token = authService.login("USER@hireben.dev", "P@ssw0rd");

    assertEquals("jwt", token);
  }

  // -----------------------------------------------------------------------------

  @Test
  void loginWithNonExistentEmail_ShouldPerformHashingAndThrowException() {
    when(userRepository.findByEmail("user@hireben.dev")).thenReturn(null);
    when(passwordEncoder.matches("P@ssw0rd", "dummyH@sh")).thenReturn(false);

    assertThrows(InvalidCredentialsException.class, () -> authService.login("USER@hireben.dev", "P@ssw0rd"));

    verify(passwordEncoder).matches("P@ssw0rd", "dummyH@sh");
  }

}

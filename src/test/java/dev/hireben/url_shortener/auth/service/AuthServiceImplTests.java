package dev.hireben.url_shortener.auth.service;

import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import dev.hireben.url_shortener.auth.repository.PermissionRepository;
import dev.hireben.url_shortener.auth.repository.UserRepository;
import dev.hireben.url_shortener.common.utility.jwt.api.JwtIssuer;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
final class AuthServiceImplTests {

  @Mock
  PasswordEncoder passwordEncoder;
  @Mock
  JwtIssuer jwtIssuer;

  @Mock
  UserRepository userRepository;
  @Mock
  PermissionRepository permissionRepository;

  final AuthServiceImpl authService;

  // =============================================================================

  AuthServiceImplTests() {
    authService = new AuthServiceImpl(passwordEncoder, jwtIssuer, userRepository, permissionRepository);
    ReflectionTestUtils.setField(authService, "tokenTtlInSec", 3600);
    ReflectionTestUtils.setField(authService, "dummyPasswordHash",
        "$2a$12$u/M/.ENM21aTNJe8OpG8m.El//zF/aNJ5Xq7tMz9GpRaAb1WxaFNW");
  }

  // =============================================================================

  // -----------------------------------------------------------------------------

}

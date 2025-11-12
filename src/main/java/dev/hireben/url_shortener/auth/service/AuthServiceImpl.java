package dev.hireben.url_shortener.auth.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import dev.hireben.url_shortener.auth.entity.User;
import dev.hireben.url_shortener.auth.exception.InvalidCredentialsException;
import dev.hireben.url_shortener.auth.exception.UserAlreadyExistsException;
import dev.hireben.url_shortener.auth.repository.PermissionRepository;
import dev.hireben.url_shortener.auth.repository.UserRepository;
import dev.hireben.url_shortener.common.utility.jwt.api.JwtIssuer;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
final class AuthServiceImpl implements AuthService {

  final PasswordEncoder passwordEncoder;
  final JwtIssuer jwtIssuer;

  final UserRepository userRepository;
  final PermissionRepository permissionRepository;

  @Value("${internal.jwt.token-ttl-in-sec}")
  Integer tokenTtlInSec;
  @Value("${internal.auth.dummy-psw-hash}")
  String dummyPasswordHash;

  // =============================================================================

  @Override
  public void register(String email, String password) {

    String emailLowerCase = email.toLowerCase();

    if (userRepository.existsByEmail(emailLowerCase)) {
      throw new UserAlreadyExistsException(String.format("A user with email %s already exists", emailLowerCase));
    }

    User newUser = User.builder()
        .email(emailLowerCase)
        .password(passwordEncoder.encode(password))
        .createdAt(Instant.now())
        .build();

    userRepository.save(newUser);
  }

  // -----------------------------------------------------------------------------

  @Override
  public String login(String email, String password) {

    String emailLowerCase = email.toLowerCase();

    User existingUser = userRepository.findByEmail(emailLowerCase);
    String hashedPassword = existingUser != null ? existingUser.getPassword() : dummyPasswordHash;

    boolean passwordMatches = passwordEncoder.matches(password, hashedPassword);

    if (existingUser == null || !passwordMatches) {
      throw new InvalidCredentialsException("Invalid email or password");
    }

    Map<String, Object> permissionMap = permissionRepository.findAll().stream()
        .collect(Collectors.toUnmodifiableMap(key -> key.getPermission(), key -> 1));

    return jwtIssuer.issueToken(
        existingUser.getId().toString(),
        null,
        permissionMap,
        Duration.ofSeconds(tokenTtlInSec),
        null);
  }

}

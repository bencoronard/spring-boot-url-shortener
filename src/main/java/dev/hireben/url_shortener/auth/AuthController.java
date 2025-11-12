package dev.hireben.url_shortener.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.hireben.url_shortener.auth.dto.LoginRequestBody;
import dev.hireben.url_shortener.auth.dto.RegisterRequestBody;
import dev.hireben.url_shortener.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
final class AuthController {

  final AuthService authService;

  // =============================================================================

  @PostMapping("/register")
  ResponseEntity<Void> register(
      @Valid @RequestBody RegisterRequestBody body) {

    authService.register(body.email(), body.password());

    return ResponseEntity.noContent().build();
  }

  // -----------------------------------------------------------------------------

  @PostMapping("/login")
  ResponseEntity<String> login(
      @Valid @RequestBody LoginRequestBody body) {

    String token = authService.login(body.email(), body.password());

    return ResponseEntity.ok().body(token);
  }

}

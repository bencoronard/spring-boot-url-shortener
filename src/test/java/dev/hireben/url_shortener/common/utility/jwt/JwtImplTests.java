package dev.hireben.url_shortener.common.utility.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import dev.hireben.url_shortener.common.utility.jwt.api.JwtIssuer;
import dev.hireben.url_shortener.common.utility.jwt.api.JwtVerifier;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
final class JwtImplTests {

  private final String issuerName = getClass().getSimpleName();
  private final KeyPair keyPair = Jwts.SIG.RS256.keyPair().build();

  // =============================================================================

  @Test
  void constructVerifierWithNullPublicKey_ShouldThrowException() {
    Exception exception = assertThrows(NullPointerException.class, () -> new JwtVerifierImpl((PublicKey) null));
    assertEquals("Public key must not be null", exception.getMessage());
  }

  // -----------------------------------------------------------------------------

  @Test
  void constructIssuerWithNullPrivateKey_ShouldThrowException() {
    Exception exception = assertThrows(NullPointerException.class,
        () -> new JwtIssuerImpl(issuerName, (PrivateKey) null));
    assertEquals("Private key must not be null", exception.getMessage());
  }

  // -----------------------------------------------------------------------------

  @Test
  void issueJwtWithPrivateKey_ShouldBeVerifiableByPublicKey() {
    JwtIssuer issuer = new JwtIssuerImpl(issuerName, keyPair.getPrivate());
    JwtVerifier verifier = new JwtVerifierImpl(keyPair.getPublic());

    String token = issuer.issueToken(null, null, null, null, null);
    Assertions.assertThat(token).isNotBlank();

    Claims claims = verifier.verifyToken(token);
    assertNotNull(claims);
    assertNotNull(claims.getId());
    assertNotNull(claims.getIssuedAt());
  }

}

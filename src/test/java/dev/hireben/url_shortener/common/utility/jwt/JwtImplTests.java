package dev.hireben.url_shortener.common.utility.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

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
  private final SecretKey symmetricKey = Jwts.SIG.HS256.key().build();
  private final KeyPair keyPair = Jwts.SIG.RS256.keyPair().build();

  // =============================================================================

  @Test
  void testJwtVerifierConstructorWithNullSymmetricKey() {
    Exception exception = assertThrows(NullPointerException.class, () -> new JwtVerifierImpl((SecretKey) null));
    assertEquals("Symmetric key must not be null", exception.getMessage());
  }

  // -----------------------------------------------------------------------------

  @Test
  void testJwtVerifierConstructorWithNullPublicKey() {
    Exception exception = assertThrows(NullPointerException.class, () -> new JwtVerifierImpl((PublicKey) null));
    assertEquals("Public key must not be null", exception.getMessage());
  }

  // -----------------------------------------------------------------------------

  @Test
  void testJwtIssuerConstructorWithNullSymmetricKey() {
    Exception exception = assertThrows(NullPointerException.class,
        () -> new JwtIssuerImpl(issuerName, (SecretKey) null));
    assertEquals("Symmetric key must not be null", exception.getMessage());
  }

  // -----------------------------------------------------------------------------

  @Test
  void testJwtIssuerConstructorWithNullPrivateKey() {
    Exception exception = assertThrows(NullPointerException.class,
        () -> new JwtIssuerImpl(issuerName, (PrivateKey) null));
    assertEquals("Private key must not be null", exception.getMessage());
  }

  // -----------------------------------------------------------------------------

  @Test
  void testIssueAndParseTokenWithoutKey() {
    JwtIssuer issuer = new JwtIssuerImpl(issuerName);
    JwtVerifier verifier = new JwtVerifierImpl();

    String token = issuer.issueToken(null, null, null, null, null);
    Assertions.assertThat(token).isNotBlank();

    Claims claims = verifier.verifyToken(token);
    assertNotNull(claims);
    assertNotNull(claims.getId());
    assertNotNull(claims.getIssuedAt());
  }

  // -----------------------------------------------------------------------------

  @Test
  void testIssueAndParseTokenWithSymmetricKey() {
    JwtIssuer issuer = new JwtIssuerImpl(issuerName, symmetricKey);
    JwtVerifier verifier = new JwtVerifierImpl(symmetricKey);

    String token = issuer.issueToken(null, null, null, null, null);
    Assertions.assertThat(token).isNotBlank();

    Claims claims = verifier.verifyToken(token);
    assertNotNull(claims);
    assertNotNull(claims.getId());
    assertNotNull(claims.getIssuedAt());
  }

  // -----------------------------------------------------------------------------

  @Test
  void testIssueAndParseTokenWithAsymmetricKeys() {
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

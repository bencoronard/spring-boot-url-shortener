package dev.hireben.url_shortener.common.utility.jwt;

import java.security.KeyPair;

import javax.crypto.SecretKey;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import dev.hireben.url_shortener.common.utility.jwt.api.JwtIssuer;
import dev.hireben.url_shortener.common.utility.jwt.api.JwtVerifier;
import io.jsonwebtoken.Jwts;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
final class JwtClientsTests {

  private final String issuerName = getClass().getSimpleName();
  private final SecretKey symmetricKey = Jwts.SIG.HS256.key().build();
  private final KeyPair keyPair = Jwts.SIG.RS256.keyPair().build();

  // =============================================================================

  @Test
  void testNewVerifier() {
    JwtVerifier expected = new JwtVerifierImpl();
    JwtVerifier actual = JwtClients.newVerifier();

    Assertions.assertThat(actual)
        .usingRecursiveComparison()
        .isEqualTo(expected);
  }

  // -----------------------------------------------------------------------------

  @Test
  void testNewVerifierWithSymmetricKey() {
    JwtVerifier expected = new JwtVerifierImpl(symmetricKey);
    JwtVerifier actual = JwtClients.newVerifierWithSymmetricKey(symmetricKey);

    Assertions.assertThat(actual)
        .usingRecursiveComparison()
        .isEqualTo(expected);
  }

  // -----------------------------------------------------------------------------

  @Test
  void testNewVerifierWithPublicKey() {
    JwtVerifier expected = new JwtVerifierImpl(keyPair.getPublic());
    JwtVerifier actual = JwtClients.newVerifierWithPublicKey(keyPair.getPublic());

    Assertions.assertThat(actual)
        .usingRecursiveComparison()
        .isEqualTo(expected);
  }

  // -----------------------------------------------------------------------------

  @Test
  void testNewIssuer() {
    JwtIssuer expected = new JwtIssuerImpl(issuerName);
    JwtIssuer actual = JwtClients.newIssuer(issuerName);

    Assertions.assertThat(actual)
        .usingRecursiveComparison()
        .isEqualTo(expected);
  }

  // -----------------------------------------------------------------------------

  @Test
  void testNewIssuerWithSymmetricKey() {
    JwtIssuer expected = new JwtIssuerImpl(issuerName, symmetricKey);
    JwtIssuer actual = JwtClients.newIssuerWithSymmetricKey(issuerName, symmetricKey);

    Assertions.assertThat(actual)
        .usingRecursiveComparison()
        .isEqualTo(expected);
  }

  // -----------------------------------------------------------------------------

  @Test
  void testNewIssuerWithPrivateKey() {
    JwtIssuer expected = new JwtIssuerImpl(issuerName, keyPair.getPrivate());
    JwtIssuer actual = JwtClients.newIssuerWithPrivateKey(issuerName, keyPair.getPrivate());

    Assertions.assertThat(actual)
        .usingRecursiveComparison()
        .isEqualTo(expected);
  }

}

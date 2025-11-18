package dev.hireben.url_shortener.common.utility.jwt;

import java.security.KeyPair;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import dev.hireben.url_shortener.common.utility.jwt.api.JwtIssuer;
import dev.hireben.url_shortener.common.utility.jwt.api.JwtVerifier;
import io.jsonwebtoken.Jwts;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
final class JwtClientsTests {

  private final String issuerName = getClass().getSimpleName();
  private final KeyPair keyPair = Jwts.SIG.RS256.keyPair().build();

  // =============================================================================

  @Test
  void newVerifierWithPublicKey_ShouldReturnValidInstance() {
    JwtVerifier expected = new JwtVerifierImpl(keyPair.getPublic());
    JwtVerifier actual = JwtClients.newVerifierWithPublicKey(keyPair.getPublic());

    Assertions.assertThat(actual)
        .usingRecursiveComparison()
        .isEqualTo(expected);
  }

  // -----------------------------------------------------------------------------

  @Test
  void newIssuerWithPrivateKey_ShouldReturnValidInstance() {
    JwtIssuer expected = new JwtIssuerImpl(issuerName, keyPair.getPrivate());
    JwtIssuer actual = JwtClients.newIssuerWithPrivateKey(issuerName, keyPair.getPrivate());

    Assertions.assertThat(actual)
        .usingRecursiveComparison()
        .isEqualTo(expected);
  }

}

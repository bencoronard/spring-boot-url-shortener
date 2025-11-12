package dev.hireben.url_shortener.common.utility.jwt;

import java.security.PrivateKey;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import javax.crypto.SecretKey;

import dev.hireben.url_shortener.common.utility.jwt.api.JwtIssuer;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;

final class JwtIssuerImpl implements JwtIssuer {

  private final Supplier<JwtBuilder> builder;

  // =============================================================================

  JwtIssuerImpl(String issuer) {
    builder = () -> Jwts.builder().issuer(issuer);
  }

  // -----------------------------------------------------------------------------

  JwtIssuerImpl(String issuer, SecretKey key) {
    Objects.requireNonNull(key, "Symmetric key must not be null");
    builder = () -> Jwts.builder().signWith(key).issuer(issuer);
  }

  // -----------------------------------------------------------------------------

  JwtIssuerImpl(String issuer, PrivateKey key) {
    Objects.requireNonNull(key, "Private key must not be null");
    builder = () -> Jwts.builder().signWith(key).issuer(issuer);
  }

  // =============================================================================

  @Override
  public String issueToken(
      String subject,
      Collection<String> audiences,
      Map<String, Object> claims,
      TemporalAmount ttl,
      Instant nbf) {

    Instant now = Instant.now();
    Instant tokenEffective = now;

    JwtBuilder jwt = builder.get();

    if (nbf != null) {
      tokenEffective = nbf;
      jwt.notBefore(Date.from(tokenEffective));
    }

    if (ttl != null) {
      Instant expAt = tokenEffective.plus(ttl);
      if (expAt.isBefore(now)) {
        throw new IllegalArgumentException("Token expiration cannot be in the past");
      }
      jwt.expiration(Date.from(expAt));
    }

    jwt.id(UUID.randomUUID().toString())
        .subject(subject)
        .issuedAt(Date.from(now));

    if (audiences != null && !audiences.isEmpty()) {
      audiences.forEach(audience -> jwt.audience().add(audience));
    }

    if (claims != null && !claims.isEmpty()) {
      jwt.claims(claims);
    }

    return jwt.compact();
  }

}

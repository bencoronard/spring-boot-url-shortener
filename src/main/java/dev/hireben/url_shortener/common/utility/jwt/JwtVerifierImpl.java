package dev.hireben.url_shortener.common.utility.jwt;

import java.security.PublicKey;
import java.util.Objects;

import dev.hireben.url_shortener.common.utility.jwt.api.JwtVerifier;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;

final class JwtVerifierImpl implements JwtVerifier {

  private final boolean secured;
  private final JwtParser parser;

  // =============================================================================

  JwtVerifierImpl(PublicKey key) {
    Objects.requireNonNull(key, "Public key must not be null");
    secured = true;
    parser = Jwts.parser().verifyWith(key).build();
  }

  // =============================================================================

  @Override
  public Claims verifyToken(String token) {
    return secured ? parser.parseSignedClaims(token).getPayload() : parser.parseUnsecuredClaims(token).getPayload();
  }

}

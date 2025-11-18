package dev.hireben.url_shortener.common.utility.jwt;

import java.security.PrivateKey;
import java.security.PublicKey;

import dev.hireben.url_shortener.common.utility.jwt.api.JwtIssuer;
import dev.hireben.url_shortener.common.utility.jwt.api.JwtVerifier;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JwtClients {

  public JwtVerifier newVerifierWithPublicKey(PublicKey key) {
    return new JwtVerifierImpl(key);
  }

  // -----------------------------------------------------------------------------

  public JwtIssuer newIssuerWithPrivateKey(String issuer, PrivateKey key) {
    return new JwtIssuerImpl(issuer, key);
  }

}

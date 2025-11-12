package dev.hireben.url_shortener.common.utility.jwt;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import dev.hireben.url_shortener.common.utility.jwt.api.JwtIssuer;
import dev.hireben.url_shortener.common.utility.jwt.api.JwtVerifier;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JwtClients {

  public JwtVerifier newVerifier() {
    return new JwtVerifierImpl();
  }

  // -----------------------------------------------------------------------------

  public JwtVerifier newVerifierWithSymmetricKey(SecretKey key) {
    return new JwtVerifierImpl(key);
  }

  // -----------------------------------------------------------------------------

  public JwtVerifier newVerifierWithPublicKey(PublicKey key) {
    return new JwtVerifierImpl(key);
  }

  // -----------------------------------------------------------------------------

  public JwtIssuer newIssuer(String issuer) {
    return new JwtIssuerImpl(issuer);
  }

  // -----------------------------------------------------------------------------

  public JwtIssuer newIssuerWithSymmetricKey(String issuer, SecretKey key) {
    return new JwtIssuerImpl(issuer, key);
  }

  // -----------------------------------------------------------------------------

  public JwtIssuer newIssuerWithPrivateKey(String issuer, PrivateKey key) {
    return new JwtIssuerImpl(issuer, key);
  }

}

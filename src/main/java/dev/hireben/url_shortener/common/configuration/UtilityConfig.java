package dev.hireben.url_shortener.common.configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import dev.hireben.url_shortener.common.utility.crypto.KeyReader;
import dev.hireben.url_shortener.common.utility.jwt.JwtClients;
import dev.hireben.url_shortener.common.utility.jwt.api.JwtIssuer;
import dev.hireben.url_shortener.common.utility.jwt.api.JwtVerifier;

@Configuration(proxyBeanMethods = false)
final class UtilityConfig {

  @Bean
  private JwtIssuer jwtIssuer(
      @Value("${spring.application.name}") String appName,
      @Value("${internal.jwt.sign-key-path}") String keyPath)
      throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

    byte[] keyFile = Files.readAllBytes(Path.of(keyPath));
    String keyBase64 = new String(keyFile);
    PrivateKey privateKey = KeyReader.readRsaPrivateKeyPkcs8(keyBase64);

    return JwtClients.newIssuerWithPrivateKey(appName, privateKey);
  }

  // -----------------------------------------------------------------------------

  @Bean
  private JwtVerifier jwtVerifier(
      @Value("${internal.jwt.verify-key-path}") String keyPath)
      throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

    byte[] keyFile = Files.readAllBytes(Path.of(keyPath));
    String keyBase64 = new String(keyFile);
    PublicKey publicKey = KeyReader.readRsaPublicKeyX509(keyBase64);

    return JwtClients.newVerifierWithPublicKey(publicKey);
  }

  // -----------------------------------------------------------------------------

  @Bean
  private PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

}

package dev.hireben.url_shortener.common.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.hireben.url_shortener.common.resolver.AuthorizationHeaderResolver;
import dev.hireben.url_shortener.common.utility.jwt.api.JwtVerifier;

@Configuration
class ResolverConfig {

  @Bean
  AuthorizationHeaderResolver authorizationHeaderResolver(JwtVerifier verifier) {
    return new AuthorizationHeaderResolver(verifier);
  }

}

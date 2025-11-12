package dev.hireben.url_shortener.common.configuration;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import dev.hireben.url_shortener.common.resolver.AuthorizationHeaderResolver;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
class WebConfig implements WebMvcConfigurer {

  private final AuthorizationHeaderResolver authHeaderResolver;

  // =============================================================================

  @Override
  public void addArgumentResolvers(@NonNull List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(authHeaderResolver);
  }

}

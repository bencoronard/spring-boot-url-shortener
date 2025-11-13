package dev.hireben.url_shortener.common.configuration;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.hireben.url_shortener.common.filter.ApiAccessLogFilter;

@Configuration(proxyBeanMethods = false)
final class FilterConfig {

  @Bean
  private FilterRegistrationBean<ApiAccessLogFilter> apiAccessLogFilter() {
    FilterRegistrationBean<ApiAccessLogFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(new ApiAccessLogFilter());
    registration.setOrder(0);
    registration.addUrlPatterns("/api/*");
    return registration;
  }

}

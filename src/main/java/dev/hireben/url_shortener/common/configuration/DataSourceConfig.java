package dev.hireben.url_shortener.common.configuration;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration(proxyBeanMethods = false)
@Profile("!test & !local")
final class DataSourceConfig {

  @Bean
  private DataSource datasource(
      @Value("${}") String pgHost,
      @Value("${}") String pgPort,
      @Value("${}") String pdDbName,
      @Value("${}") String pgUsername,
      @Value("${}") String pgPassword,
      @Value("${spring.datasource.hikari.pool-name}") String cpPoolName,
      @Value("${spring.datasource.hikari.maximum-pool-size}") Integer cpPoolSize,
      @Value("${spring.datasource.hikari.minimum-idle}") Integer cpPoolMinIdle,
      @Value("${spring.datasource.hikari.idle-timeout}") Integer cpIdleTimeout,
      @Value("${spring.datasource.hikari.connection-timeout}") Integer cpConnTimeout,
      @Value("${spring.datasource.hikari.max-lifetime}") Integer cpMaxLifeTime) {

    HikariConfig config = new HikariConfig();
    config.setDriverClassName("org.postgresql.Driver");
    config.setJdbcUrl(String.format("jdbc:postgresql://%s:%s/%s", pgHost, pgPort, pdDbName));
    config.setUsername(pgUsername);
    config.setPassword(pgPassword);

    config.setPoolName(cpPoolName);
    config.setMaximumPoolSize(cpPoolSize);
    config.setMinimumIdle(cpPoolMinIdle);
    config.setIdleTimeout(cpIdleTimeout);
    config.setConnectionTimeout(cpConnTimeout);
    config.setMaxLifetime(cpMaxLifeTime);

    return new HikariDataSource(config);
  }

}

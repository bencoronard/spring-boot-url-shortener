package dev.hireben.url_shortener.common.utility.jwt.api;

import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Collection;
import java.util.Map;

public interface JwtIssuer {

  String issueToken(
      String subject,
      Collection<String> audiences,
      Map<String, Object> claims,
      TemporalAmount ttl,
      Instant nbf);

}

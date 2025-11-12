package dev.hireben.url_shortener.common.utility.jwt.api;

import io.jsonwebtoken.Claims;

public interface JwtVerifier {

  Claims verifyToken(String token);

}

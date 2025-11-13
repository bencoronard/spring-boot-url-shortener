package dev.hireben.url_shortener.common.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import dev.hireben.url_shortener.common.annotation.AuthorizationHeader;
import dev.hireben.url_shortener.common.constant.MessageHeader;
import dev.hireben.url_shortener.common.exception.TokenMalformedException;
import dev.hireben.url_shortener.common.utility.jwt.api.JwtVerifier;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class AuthorizationHeaderResolver implements HandlerMethodArgumentResolver {

  private final JwtVerifier verifier;

  // =============================================================================

  @Override
  public boolean supportsParameter(@NonNull MethodParameter parameter) {
    return parameter.getParameterType().equals(Claims.class)
        && parameter.hasParameterAnnotation(AuthorizationHeader.class);
  }

  // -----------------------------------------------------------------------------

  @Override
  public Object resolveArgument(
      @NonNull MethodParameter parameter,
      @Nullable ModelAndViewContainer mavContainer,
      @NonNull NativeWebRequest webRequest,
      @Nullable WebDataBinderFactory binderFactory) throws Exception {

    String header = webRequest.getHeader(MessageHeader.AUTHORIZATION);

    if (header == null || header.isBlank()) {
      throw new MissingRequestHeaderException(MessageHeader.AUTHORIZATION, parameter);
    }

    Claims claims = verifier.verifyToken(header.substring("Bearer ".length()));
    String subject = claims.getSubject();

    if (subject == null || subject.isBlank() || !subject.matches("^\\d+$")) {
      throw new TokenMalformedException("Token is malformed");
    }

    return claims;
  }

}

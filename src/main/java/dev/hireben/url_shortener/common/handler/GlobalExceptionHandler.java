package dev.hireben.url_shortener.common.handler;

import java.net.SocketTimeoutException;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import dev.hireben.url_shortener.common.exception.ApplicationException;
import io.jsonwebtoken.JwtException;
import io.micrometer.tracing.Tracer;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@RequiredArgsConstructor
final class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  final Tracer tracer;

  static final Map<Class<? extends Throwable>, HttpStatus> exceptionStatusMap = Map.of();

  // =============================================================================

  @Override
  protected final @NonNull ResponseEntity<Object> createResponseEntity(
      @Nullable Object body,
      @NonNull HttpHeaders headers,
      @NonNull HttpStatusCode statusCode,
      @NonNull WebRequest request) {

    if (body instanceof ProblemDetail problemDetail) {
      problemDetail.setProperty("timestamp", Instant.now());
      problemDetail.setProperty("trace", tracer.currentTraceContext().context().traceId());
    }

    return super.createResponseEntity(body, headers, statusCode, request);
  }

  // =============================================================================

  @Override
  protected final ResponseEntity<Object> handleMethodArgumentNotValid(
      @NonNull MethodArgumentNotValidException ex,
      @NonNull HttpHeaders headers,
      @NonNull HttpStatusCode status,
      @NonNull WebRequest request) {

    ProblemDetail problemDetail = ex.updateAndGetBody(getMessageSource(), LocaleContextHolder.getLocale());

    Collection<FieldValidationErrorMap> errors = ex.getBindingResult().getAllErrors().stream()
        .map(error -> FieldValidationErrorMap.builder()
            .field(((FieldError) error).getField())
            .message(error.getDefaultMessage())
            .build())
        .toList();

    problemDetail.setProperty("errors", errors);

    return createResponseEntity(problemDetail, headers, status, request);
  }

  // -----------------------------------------------------------------------------

  @ExceptionHandler(ConstraintViolationException.class)
  ResponseEntity<Object> handleConstraintViolation(
      ConstraintViolationException ex,
      WebRequest request) {

    HttpStatus status = HttpStatus.BAD_REQUEST;

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, "Validation failed");

    Collection<FieldValidationErrorMap> errors = ex.getConstraintViolations().stream()
        .map(error -> FieldValidationErrorMap.builder()
            .field(error.getPropertyPath().toString())
            .message(error.getMessage())
            .build())
        .toList();

    problemDetail.setProperty("errors", errors);

    return createResponseEntity(problemDetail, HttpHeaders.EMPTY, status, request);
  }

  // -----------------------------------------------------------------------------

  @ExceptionHandler(MissingRequestHeaderException.class)
  ResponseEntity<Object> handleMissingRequestHeader(
      MissingRequestHeaderException ex,
      WebRequest request) {

    HttpStatus status = HttpStatus.BAD_REQUEST;

    String message = String.format("Missing HTTP header: %s", ex.getHeaderName());

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, message);

    return createResponseEntity(problemDetail, HttpHeaders.EMPTY, status, request);
  }

  // -----------------------------------------------------------------------------

  @ExceptionHandler(SocketTimeoutException.class)
  ResponseEntity<Object> handleSocketTimeout(
      SocketTimeoutException ex,
      WebRequest request) {

    HttpStatus status = HttpStatus.BAD_REQUEST;

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, "Gateway timed out");

    return createResponseEntity(problemDetail, HttpHeaders.EMPTY, status, request);
  }

  // -----------------------------------------------------------------------------

  @ExceptionHandler(JwtException.class)
  ResponseEntity<Object> handleJwtValidationFailure(
      JwtException ex,
      WebRequest request) {

    HttpStatus status = HttpStatus.UNAUTHORIZED;

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, "Authorization failed");

    return createResponseEntity(problemDetail, HttpHeaders.EMPTY, status, request);
  }

  // -----------------------------------------------------------------------------

  @ExceptionHandler({
      OptimisticLockingFailureException.class,
      PessimisticLockingFailureException.class
  })
  ResponseEntity<Object> handleConcurrencyFailure(
      ConcurrencyFailureException ex,
      WebRequest request) {

    HttpStatus status = HttpStatus.CONFLICT;

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, "Database update fail. Try again later.");

    return createResponseEntity(problemDetail, HttpHeaders.EMPTY, status, request);
  }

  // -----------------------------------------------------------------------------

  @ExceptionHandler(ApplicationException.class)
  ResponseEntity<Object> handleApplicationException(
      ApplicationException ex,
      WebRequest request) {

    HttpStatus status = exceptionStatusMap.getOrDefault(ex.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());

    return createResponseEntity(problemDetail, HttpHeaders.EMPTY, status, request);
  }

}

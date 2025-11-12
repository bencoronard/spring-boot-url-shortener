package dev.hireben.url_shortener.common.exception;

public abstract class ApplicationException extends RuntimeException {

  protected ApplicationException(String message) {
    super(message);
  }

}

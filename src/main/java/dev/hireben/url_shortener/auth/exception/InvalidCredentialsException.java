package dev.hireben.url_shortener.auth.exception;

import dev.hireben.url_shortener.common.exception.ApplicationException;

public class InvalidCredentialsException extends ApplicationException {

  public InvalidCredentialsException(String message) {
    super(message);
  }

}

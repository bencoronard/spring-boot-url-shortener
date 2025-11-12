package dev.hireben.url_shortener.auth.exception;

import dev.hireben.url_shortener.common.exception.ApplicationException;

public class UserAlreadyExistsException extends ApplicationException {

  public UserAlreadyExistsException(String message) {
    super(message);
  }

}

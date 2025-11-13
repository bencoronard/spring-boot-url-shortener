package dev.hireben.url_shortener.url.exception;

import dev.hireben.url_shortener.common.exception.ApplicationException;

public class UrlShortenExceedMaxAttemptsException extends ApplicationException {

  public UrlShortenExceedMaxAttemptsException(String message) {
    super(message);
  }

}

package dev.hireben.url_shortener.url.exception;

import dev.hireben.url_shortener.common.exception.ApplicationException;

public class UrlMappingNotFoundException extends ApplicationException {

  public UrlMappingNotFoundException(String message) {
    super(message);
  }

}

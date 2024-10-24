package com.orange.proxywebclient;

import io.vertx.mutiny.core.http.HttpServerRequest;

public interface ConfigAuthorization {

  boolean isAuthorized(HttpServerRequest request);

  default void checkAuthorized(HttpServerRequest request) {
    if (!isAuthorized(request)) {
      throw new UnauthorizedException();
    }
  }

  public static class UnauthorizedException extends SecurityException {
    public UnauthorizedException() {}

    public UnauthorizedException(String errorMessage) {
      this(errorMessage, null);
    }

    public UnauthorizedException(Throwable cause) {
      this(null, cause);
    }

    public UnauthorizedException(String errorMessage, Throwable cause) {
      super(errorMessage, cause);
    }
  }
}

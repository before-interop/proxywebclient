package com.orange.proxywebclient;

import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.http.HttpServerRequest;
import java.util.Map;

public interface AuthenticationMethod {

  /**
   * Route the request to the partner
   *
   * @param partner
   * @param request
   * @return the response from the partner
   */
  Uni<Buffer> proxy(
      Partner partner, HttpServerRequest request, Buffer body, RoutingContext routingContext);

  /**
   * Validate the configuration
   *
   * <p>You can override this method to validate the configuration with Hibernate Validator
   *
   * <p>example:
   *
   * <pre>{@code
   * Partner.validateConfig(config, MyCustomConfig.class);
   * }</pre>
   *
   * @throws jakarta.validation.ConstraintViolationException if the configuration is invalid
   */
  default void validate(Map<String, Object> config) {
    // override this method to validate the configuration
  }

  /**
   * Validate the configuration
   *
   * @throws jakarta.validation.ConstraintViolationException if the configuration is invalid
   */
  default void validate(Partner partner) {
    validate(partner.getConfig());
  }
}

package com.orange.proxywebclient.runtime;

import com.orange.proxywebclient.Proxy;
import io.quarkus.vertx.web.*;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.http.HttpServerRequest;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

public class CustomRoutes {
  private static final Logger log = Logger.getLogger(CustomRoutes.class);
  @Inject Proxy proxy;
  @Inject RoutingContext routingContext;

  @Route(regex = "^\\/api\\/(?<clientId>[^\\/]+)\\/(?<name>[^\\/]+)\\/?(?<path>.*)", order = -1)
  public Uni<Buffer> proxyOrangeV1(
      HttpServerRequest request, @Body io.vertx.core.buffer.Buffer body) {
    var nameApi = request.getParam("name");
    log.infof("api call to %s", nameApi);
    var path = request.getParam("path");
    request.params().set("path", nameApi + "/" + path);
    return proxy.proxy(request, body, routingContext);
  }
}

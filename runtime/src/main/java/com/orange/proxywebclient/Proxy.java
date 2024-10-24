package com.orange.proxywebclient;

import io.quarkus.arc.Arc;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.http.HttpServerRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

@ApplicationScoped
public class Proxy {

  private static final Logger log = Logger.getLogger(Proxy.class);

  private static final HashMap<String, Class<?>> classList = new HashMap<>();

  private static final List<String> packageList = new ArrayList<>();

  @Inject Partner.Service partnerService;

  @Inject ProxyConfig config;

  public static class NotImplementedException extends RuntimeException {
    public NotImplementedException(String clientId) {
      super(clientId);
    }
  }

  public Uni<Buffer> proxy(
      HttpServerRequest request, io.vertx.core.buffer.Buffer body, RoutingContext routingContext) {
    return proxy(request, new Buffer(body), routingContext);
  }

  public Uni<Buffer> proxy(HttpServerRequest request, Buffer body, RoutingContext routingContext) {
    var clientId = request.getParam("clientId");
    MDC.put("clientId", clientId);

    log.info("proxy request for clientId: " + clientId);

    return partnerService
        .find(clientId)
        .onItem()
        .ifNull()
        .failWith(new NotImplementedException("No partner found for clientId: " + clientId))
        .onItem()
        .transformToUni(p -> getAuthenticationMethod(p).proxy(p, request, body, routingContext));
  }

  public AuthenticationMethod getAuthenticationMethod(Partner partner) {
    return (AuthenticationMethod)
        Arc.container().instance(getAuthMethodClass(partner.getMethod())).get();
  }

  private List<String> getPackageList() {
    if (!packageList.isEmpty()) {
      return packageList;
    }

    var packages = config.packages.orElse("") + ",,com.orange.proxywebclient.impl.method";

    Arrays.stream(packages.split(","))
        .distinct()
        .map(s -> s.isBlank() ? s : s + ".")
        .forEach(packageList::add);

    return packageList;
  }

  private Class<?> getAuthMethodClass(String className) {
    if (classList.containsKey(className)) {
      return classList.get(className);
    }

    log.debug("packageList: " + getPackageList());

    for (var p : getPackageList()) {
      try {
        log.debug("Try to load authentication method for " + p + className);
        var c = Class.forName(p + className);
        if (AuthenticationMethod.class.isAssignableFrom(c)) {
          classList.put(className, c);
          return c;
        }
      } catch (Exception e) {
        log.debug("No authentication method found for " + p + className);
      }
    }

    throw new NotImplementedException("No authentication method found for " + className);
  }
}

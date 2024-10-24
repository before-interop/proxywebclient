package com.orange.proxywebclient.runtime;

import com.orange.proxywebclient.Cache;
import com.orange.proxywebclient.ConfigAuthorization;
import com.orange.proxywebclient.Partner;
import com.orange.proxywebclient.Proxy;
import io.quarkus.vertx.web.Body;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.Route.HandlerType;
import io.quarkus.vertx.web.Route.HttpMethod;
import io.quarkus.vertx.web.runtime.ValidationSupport;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.http.HttpServerRequest;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import java.util.List;
import org.jboss.logging.Logger;

public class Routes {

  private static final Logger log = Logger.getLogger(Routes.class);

  @Inject Proxy proxy;

  @Inject Partner.Service partnerService;

  @Inject ConfigAuthorization configAuthorization;

  @Inject Cache cache;
  @Inject RoutingContext routingContext;

  @Route(type = HandlerType.FAILURE)
  void handleError(Proxy.NotImplementedException e, RoutingContext rc) {
    log.error("configuration missing: " + e.getMessage());
    rc.response().setStatusCode(501).end();
  }

  @Route(type = HandlerType.FAILURE)
  void handleError(Partner.PartnerNotFoundException e, RoutingContext rc) {
    log.error("Partner not found", e);
    rc.response().setStatusCode(404).end();
  }

  @Route(type = HandlerType.FAILURE)
  void handleError(ConfigAuthorization.UnauthorizedException e, RoutingContext rc) {
    log.error("Unauthorized", e);
    rc.response().setStatusCode(401).end();
  }

  @Route(type = HandlerType.FAILURE)
  void handleError(ConstraintViolationException e, RoutingContext rc) {
    ValidationSupport.handleViolationException(e, rc, false);
  }

  @Route(type = HandlerType.FAILURE, order = 1000)
  void handleError(Exception e, RoutingContext rc) {
    // do not expose internal error to client
    log.fatal("unexpected error", e);
    rc.response().setStatusCode(500).end();
  }

  @Route(regex = "^\\/proxy\\/(?<clientId>[^\\/]+)\\/?(?<path>.*)", order = -1)
  public Uni<Buffer> proxy(HttpServerRequest request, @Body io.vertx.core.buffer.Buffer body) {
    return proxy.proxy(request, body, routingContext);
  }

  @Route(path = "/partners", methods = HttpMethod.GET)
  public Uni<List<Partner>> getPartner(HttpServerRequest request) {
    configAuthorization.checkAuthorized(request);
    return partnerService.list();
  }

  @Route(path = "/partners", methods = HttpMethod.PUT)
  public Uni<Partner> upsertPartner(@Valid @Body Partner partner, HttpServerRequest request) {
    configAuthorization.checkAuthorized(request);
    proxy.getAuthenticationMethod(partner).validate(partner);
    return partnerService
        .upsert(partner)
        .onItem()
        .transformToUni(p -> cache.clear(partner.getClientId()).map(v -> p));
  }

  @Route(path = "/partners/:clientId", methods = HttpMethod.DELETE)
  public Uni<Void> deletePartner(HttpServerRequest request) {
    configAuthorization.checkAuthorized(request);
    var clientId = request.getParam("clientId");
    return partnerService.delete(clientId).onItem().transformToUni(v -> cache.clear(clientId));
  }

  @Route(path = "/partners/:clientId", methods = HttpMethod.GET)
  public Uni<Partner> findPartner(HttpServerRequest request) {
    configAuthorization.checkAuthorized(request);
    var clientId = request.getParam("clientId");
    return partnerService
        .find(clientId)
        .onItem()
        .ifNull()
        .failWith(() -> new Partner.PartnerNotFoundException(clientId));
  }
}

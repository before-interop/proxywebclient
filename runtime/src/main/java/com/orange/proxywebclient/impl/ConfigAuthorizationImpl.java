package com.orange.proxywebclient.impl;

import com.orange.proxywebclient.ConfigAuthorization;
import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.Unremovable;
import io.vertx.mutiny.core.http.HttpServerRequest;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@DefaultBean
@Unremovable
public class ConfigAuthorizationImpl implements ConfigAuthorization {

  @Override
  public boolean isAuthorized(HttpServerRequest request) {
    return true;
  }
}

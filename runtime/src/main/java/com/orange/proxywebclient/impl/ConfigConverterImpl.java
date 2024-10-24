package com.orange.proxywebclient.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orange.proxywebclient.Partner;
import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.Unremovable;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Map;

@ApplicationScoped
@DefaultBean
@Unremovable
public class ConfigConverterImpl implements Partner.ConfigConverter {

  @Inject ObjectMapper om;

  @Override
  public <T> T cast(Map<String, Object> config, Class<T> clazz) {
    try {
      var json = new JsonObject(config).encode();
      return om.readValue(json, clazz);
    } catch (Exception e) {
      throw new Partner.ConfigConverterException(e);
    }
  }
}

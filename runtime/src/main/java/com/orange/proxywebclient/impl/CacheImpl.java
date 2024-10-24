package com.orange.proxywebclient.impl;

import com.orange.proxywebclient.Cache;
import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.Unremovable;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.HashMap;

@ApplicationScoped
@DefaultBean
@Unremovable
public class CacheImpl implements Cache {

  private static final HashMap<String, HashMap<String, String>> cache = new HashMap<>();

  @Override
  public Uni<Void> clear() {
    cache.clear();
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<Void> clear(String clientId) {
    if (cache.containsKey(clientId)) {
      cache.get(clientId).clear();
    }

    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<String> put(String clientId, String key, String value) {
    cache.computeIfAbsent(clientId, k -> new HashMap<>());

    cache.get(clientId).put(key, value);

    return Uni.createFrom().item(value);
  }

  @Override
  public Uni<String> get(String clientId, String key) {
    if (cache.containsKey(clientId)) {
      return Uni.createFrom().item(cache.get(clientId).get(key));
    }

    return Uni.createFrom().nullItem();
  }

  @Override
  public Uni<Boolean> contains(String clientId, String key) {
    if (cache.containsKey(clientId)) {
      return Uni.createFrom().item(cache.get(clientId).containsKey(key));
    }

    return Uni.createFrom().item(false);
  }

  @Override
  public Uni<Void> remove(String clientId, String key) {
    if (cache.containsKey(clientId)) {
      cache.get(clientId).remove(key);
    }

    return Uni.createFrom().voidItem();
  }
}

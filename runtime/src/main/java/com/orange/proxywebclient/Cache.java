package com.orange.proxywebclient;

import io.smallrye.mutiny.Uni;

public interface Cache {

  /** Clear the cache */
  Uni<Void> clear();

  /** Clear the cache for a given client */
  Uni<Void> clear(String clientId);

  /** Clear the cache for a given client */
  default Uni<Void> clear(Partner partner) {
    return clear(partner.getClientId());
  }

  /** Put a value in the cache */
  Uni<String> put(String clientId, String key, String value);

  /** Put a value in the cache */
  default Uni<String> put(Partner partner, String key, String value) {
    return put(partner.getClientId(), key, value);
  }

  /** Put a value in the cache */
  default Uni<String> put(Partner partner, String key, Object value) {
    return put(partner, key, value.toString());
  }

  /** Put a value in the cache */
  default Uni<String> put(String clientId, String key, Object value) {
    return put(clientId, key, value.toString());
  }

  /** Get a value from the cache */
  Uni<String> get(String clientId, String key);

  /** Get a value from the cache */
  default Uni<String> get(Partner partner, String key) {
    return get(partner.getClientId(), key);
  }

  /** Check if a key is in the cache */
  Uni<Boolean> contains(String clientId, String key);

  /** Check if a key is in the cache */
  default Uni<Boolean> contains(Partner partner, String key) {
    return contains(partner.getClientId(), key);
  }

  /** Remove a key from the cache */
  Uni<Void> remove(String clientId, String key);

  /** Remove a key from the cache */
  default Uni<Void> remove(Partner partner, String key) {
    return remove(partner.getClientId(), key);
  }
}

package com.orange.proxywebclient.impl;

import com.orange.proxywebclient.Partner;
import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.Unremovable;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.List;

@ApplicationScoped
@DefaultBean
@Unremovable
public class PartnerServiceImpl implements Partner.Service {

  private static final HashMap<String, Partner> partners = new HashMap<>();

  @Override
  public Uni<Partner> upsert(Partner partner) {
    partners.put(partner.getClientId(), partner);
    return Uni.createFrom().item(partner);
  }

  @Override
  public Uni<Void> delete(String clientId) {
    partners.remove(clientId);
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<Partner> find(String clientId) {
    if (partners.containsKey(clientId)) {
      return Uni.createFrom().item(partners.get(clientId));
    }

    return Uni.createFrom().nullItem();
  }

  @Override
  public Uni<List<Partner>> list() {
    return Uni.createFrom().item(List.copyOf(partners.values()));
  }
}

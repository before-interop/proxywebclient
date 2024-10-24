package app;

import com.orange.proxywebclient.Partner;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("java:S3252")
@ApplicationScoped
public class PartnerService implements Partner.Service {

  @Override
  @WithTransaction
  public Uni<Partner> upsert(Partner partner) {
    return PartnerEntity.findById(partner.getClientId())
        .onItem()
        .ifNull()
        .continueWith(PartnerEntity.fromPartner(partner))
        .map(PartnerEntity.class::cast)
        .onItem()
        .ifNotNull()
        .transformToUni(PartnerEntity::persistAndFlush)
        .map(p -> ((PartnerEntity) p).toPartner());
  }

  @Override
  @WithTransaction
  public Uni<Void> delete(String clientId) {
    return PartnerEntity.deleteById(clientId)
        .onItem()
        .transformToUni(i -> Uni.createFrom().voidItem());
  }

  @Override
  @WithSession
  public Uni<Partner> find(String clientId) {
    return PartnerEntity.findById(clientId)
        .onItem()
        .ifNotNull()
        .transform(p -> ((PartnerEntity) p).toPartner());
  }

  @Override
  public Uni<List<Partner>> list() {
    return PartnerEntity.listAll()
        .map(
            l -> {
              List<Partner> list = new ArrayList<>();
              l.forEach(p -> list.add(((PartnerEntity) p).toPartner()));
              return list;
            });
  }
}

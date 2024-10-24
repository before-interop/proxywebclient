package app;

import com.orange.proxywebclient.Cache;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@SuppressWarnings({"java:S3252", "java:S1192"})
@ApplicationScoped
public class CacheService implements Cache {

  @Override
  @WithTransaction
  public Uni<Void> clear() {
    return CacheEntity.deleteAll().onItem().transformToUni(i -> Uni.createFrom().voidItem());
  }

  @Override
  @WithTransaction
  public Uni<Void> clear(String clientId) {
    return CacheEntity.delete("id.clientId", clientId)
        .onItem()
        .transformToUni(i -> Uni.createFrom().voidItem());
  }

  @Override
  @WithTransaction
  public Uni<String> put(String clientId, String key, String value) {
    var entity = new CacheEntity();
    entity.id.clientId = clientId;
    entity.id.key = key;
    entity.value = value;

    return entity.persistAndFlush().map(i -> value);
  }

  @Override
  @WithSession
  public Uni<String> get(String clientId, String key) {
    return CacheEntity.findById(new CacheEntity.CacheId(clientId, key))
        .onItem()
        .ifNotNull()
        .transform(c -> ((CacheEntity) c).value);
  }

  @Override
  public Uni<Boolean> contains(String clientId, String key) {
    return CacheEntity.findById(new CacheEntity.CacheId(clientId, key))
        .onItem()
        .ifNotNull()
        .transform(c -> true)
        .onItem()
        .ifNull()
        .continueWith(false);
  }

  @Override
  @WithTransaction
  public Uni<Void> remove(String clientId, String key) {
    return CacheEntity.deleteById(new CacheEntity.CacheId(clientId, key))
        .onItem()
        .transformToUni(i -> Uni.createFrom().voidItem());
  }
}

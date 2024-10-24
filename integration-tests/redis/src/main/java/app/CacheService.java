package app;

import com.orange.proxywebclient.Cache;
import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.hash.ReactiveHashCommands;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CacheService implements Cache {

  private final ReactiveHashCommands<String, String, String> commands;

  CacheService(ReactiveRedisDataSource ds) {
    commands = ds.hash(String.class);
  }

  @Override
  public Uni<Void> clear() {
    // not implemented
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<Void> clear(String clientId) {
    return commands.hdel(clientId, "*").onItem().transformToUni(i -> Uni.createFrom().voidItem());
  }

  @Override
  public Uni<String> put(String clientId, String key, String value) {
    return commands
        .hset(clientId, key, value)
        .onItem()
        .transformToUni(i -> Uni.createFrom().item(value));
  }

  @Override
  public Uni<String> get(String clientId, String key) {
    return commands.hget(clientId, key);
  }

  @Override
  public Uni<Boolean> contains(String clientId, String key) {
    return commands.hexists(clientId, key);
  }

  @Override
  public Uni<Void> remove(String clientId, String key) {
    return commands.hdel(clientId, key).onItem().transformToUni(i -> Uni.createFrom().voidItem());
  }
}

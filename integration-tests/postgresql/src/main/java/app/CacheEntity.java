package app;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import java.io.Serializable;

@Entity(name = "cache")
public class CacheEntity extends PanacheEntityBase {

  @EmbeddedId public CacheId id = new CacheId();

  public String value;

  @Embeddable
  public static class CacheId implements Serializable {
    public String clientId;
    public String key;

    CacheId() {}

    public CacheId(String clientId, String key) {
      this.clientId = clientId;
      this.key = key;
    }
  }
}

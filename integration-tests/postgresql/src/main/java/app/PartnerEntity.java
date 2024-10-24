package app;

import com.orange.proxywebclient.Partner;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.vertx.core.json.JsonObject;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "partner")
public class PartnerEntity extends PanacheEntityBase {

  @Id public String clientId;

  @Column(columnDefinition = "TEXT")
  public String data;

  public static PartnerEntity fromPartner(Partner partner) {
    var entity = new PartnerEntity();
    entity.data = JsonObject.mapFrom(partner).encode();
    entity.clientId = partner.getClientId();

    return entity;
  }

  public Partner toPartner() {
    return new JsonObject(data).mapTo(Partner.class);
  }
}

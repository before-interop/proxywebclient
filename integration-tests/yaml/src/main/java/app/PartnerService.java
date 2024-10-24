package app;

import com.orange.proxywebclient.Partner;
import com.orange.proxywebclient.Proxy;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.yaml.snakeyaml.Yaml;

@ApplicationScoped
public class PartnerService implements Partner.Service {

  private static final Logger log = Logger.getLogger(PartnerService.class);
  private static final List<Partner> PARTNERS = new ArrayList<>();

  @ConfigProperty(name = "proxy.partners")
  String configPartnersLocation;

  private Validator validator;
  private Proxy proxy;

  public PartnerService(Validator validator, Proxy proxy) {
    this.validator = validator;
    this.proxy = proxy;
  }

  void onStart(@Observes StartupEvent ev) {
    Map<String, Object> yaml = new Yaml().load(configPartnersLocation);

    yaml.forEach(
        (k, v) -> {
          var partner = JsonObject.mapFrom(v).mapTo(Partner.class);
          partner.setClientId(k);

          try {
            var violations = validator.validate(partner);
            if (!violations.isEmpty()) {
              throw new ConstraintViolationException(violations);
            }
          } catch (ConstraintViolationException e) {
            log.fatalf("configuration error for partner `%s`: %s", k, e.getMessage());
            throw e;
          }

          try {
            proxy.getAuthenticationMethod(partner).validate(partner);
          } catch (ConstraintViolationException e) {
            log.fatalf(
                "configuration error for partner `%s` in `config` property: %s", k, e.getMessage());
            throw e;
          }

          PARTNERS.add(partner);
        });
  }

  @Override
  public Uni<Partner> upsert(Partner partner) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public Uni<Void> delete(String clientId) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public Uni<Partner> find(String clientId) {
    return Uni.createFrom()
        .item(
            PARTNERS.stream()
                .filter(p -> p.getClientId().equals(clientId))
                .findFirst()
                .orElse(null));
  }

  @Override
  public Uni<List<Partner>> list() {
    return Uni.createFrom().item(PARTNERS);
  }
}

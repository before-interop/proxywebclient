package com.orange.proxywebclient.impl.method;

import com.orange.proxywebclient.AuthenticationMethodOauth2;
import com.orange.proxywebclient.Partner;
import com.orange.proxywebclient.ProxyAuthMethod;
import com.orange.proxywebclient.impl.ConfigPartnerAuth;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.MultiMap;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@ProxyAuthMethod
public class Bouygues extends AuthenticationMethodOauth2 {

  @RegisterForReflection
  public static class BouyguesConfig extends ConfigPartnerAuth {
    private boolean sandbox = false;

    public boolean isSandbox() {
      return sandbox;
    }

    public ConfigPartnerAuth setSandbox(boolean sandbox) {
      this.sandbox = sandbox;
      return this;
    }

    @Override
    public Optional<String> getTokenUrl() {
      return tokenUrl.or(
          () ->
              Optional.of(
                  isSandbox()
                      ? "https://oauth2.sandbox.bouyguestelecom.fr/ap2/token"
                      : "https://oauth2.bouyguestelecom.fr/token"));
    }

    @Override
    public Optional<String> getGrantType() {
      return grantType.or(() -> Optional.of("client_credentials"));
    }
  }

  @Override
  public void validate(Map<String, Object> config) {
    Partner.validateConfig(config, BouyguesConfig.class);
  }

  @Override
  protected Uni<HttpResponse<Buffer>> getTokenFromApi() {
    var config = getPartner().getConfig(BouyguesConfig.class);
    var request =
        getClientRequest(HttpMethod.POST, config.getTokenUrl().orElseThrow())
            .basicAuthentication(config.getLogin(), config.getPassword());

    var form =
        MultiMap.caseInsensitiveMultiMap().set("grant_type", config.getGrantType().orElseThrow());

    return request.sendForm(form);
  }

  @Override
  protected Token toToken(JsonObject json) {
    var now = Instant.now().getEpochSecond();

    return new Token()
        .setIat(now)
        .setExp(now + json.getLong("expires_in"))
        .setToken(json.getString("access_token"));
  }
}

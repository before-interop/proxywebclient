package com.orange.proxywebclient.impl.method;

import com.orange.proxywebclient.*;
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
public class Backbone extends AuthenticationMethodOauth2 {

  @RegisterForReflection
  public static class BackboneConfig extends ConfigPartnerAuth {

    private boolean inside = false;
    private String HeaderAccept = "application/json";

    @Override
    public Optional<String> getTokenUrl() {
      return tokenUrl.or(() -> Optional.of(getBaseUrl() + "/oauth/v3/token"));
    }

    public boolean isInside() {
      return inside;
    }

    public ConfigPartnerAuth setInside(boolean inside) {
      this.inside = inside;
      return this;
    }

    public String getBaseUrl() {
      return inside ? "https://inside01.api.intraorange" : "https://api.orange.com";
    }

    @Override
    public Optional<String> getGrantType() {
      return grantType.or(() -> Optional.of("client_credentials"));
    }

    public String getHeaderAccept() {
      return HeaderAccept;
    }

    public void setHeaderAccept(String headerAccept) {
      HeaderAccept = headerAccept;
    }
  }

  @Override
  public void validate(Map<String, Object> config) {
    Partner.validateConfig(config, BackboneConfig.class);
  }

  @Override
  protected String getBaseUrl() {
    var baseUrl =
        getPartner()
            .getBaseUrl()
            .orElseThrow(
                () ->
                    new Proxy.NotImplementedException(
                        "baseUrl should contain a complete url or service name and version"));

    if (baseUrl.startsWith("http")) {
      return baseUrl;
    }

    var b = getPartner().getConfig(BackboneConfig.class).getBaseUrl();

    return baseUrl.startsWith("/") ? b + baseUrl : b + "/" + baseUrl;
  }

  @Override
  protected Uni<HttpResponse<Buffer>> getTokenFromApi() {
    var config = getPartner().getConfig(BackboneConfig.class);
    var request =
        getClientRequest(HttpMethod.POST, config.getTokenUrl().orElseThrow())
            .basicAuthentication(config.getLogin(), config.getPassword())
            .putHeader("Accept", config.getHeaderAccept());

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

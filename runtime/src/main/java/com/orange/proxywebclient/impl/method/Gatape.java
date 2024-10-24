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
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ProxyAuthMethod
public class Gatape extends AuthenticationMethodOauth2 {

  @RegisterForReflection
  public static class GatApeConfig extends ConfigPartnerAuth {
    private Optional<List<String>> scopes = Optional.empty();
    private boolean internal = true;

    public boolean isInternal() {
      return internal;
    }

    private String headerAccept = "application/json;charset=utf-8";

    private String headerContentType = "application/x-www-form-urlencoded";

    public ConfigPartnerAuth setInternal(boolean usePrivate) {
      this.internal = usePrivate;
      return this;
    }

    public Optional<List<String>> getScopes() {
      return scopes;
    }

    public ConfigPartnerAuth setScopes(Optional<List<String>> scopes) {
      this.scopes = scopes;
      return this;
    }

    @Override
    public Optional<String> getTokenUrl() {
      return tokenUrl.or(
          () ->
              Optional.of(
                  isInternal()
                      ? "https://okapi-v2.api.hbx.geo.intra.ftgroup/v2/token"
                      : "https://okapi-v2-public.apigw.orange.fr/v2/token"));
    }

    @Override
    public Optional<String> getGrantType() {
      return grantType.or(() -> Optional.of("client_credentials"));
    }

    public String getHeaderAccept() {
      return headerAccept;
    }

    public void setHeaderAccept(String headerAccept) {
      this.headerAccept = headerAccept;
    }

    public String getHeaderContentType() {
      return headerContentType;
    }

    public void setHeaderContentType(String headerContentType) {
      this.headerContentType = headerContentType;
    }
  }

  @Override
  public void validate(Map<String, Object> config) {
    Partner.validateConfig(config, GatApeConfig.class);
  }

  @Override
  protected Uni<HttpResponse<Buffer>> getTokenFromApi() {
    var config = getPartner().getConfig(GatApeConfig.class);
    var request = getClientRequest(HttpMethod.POST, config.getTokenUrl().orElseThrow());

    request
        .headers()
        .add("Content-Type", config.headerContentType)
        .add("Accept", config.headerAccept);

    var form =
        MultiMap.caseInsensitiveMultiMap()
            .set("grant_type", config.getGrantType().orElseThrow())
            .set("client_id", config.getLogin())
            .set("client_secret", config.getPassword());
    if (config.getScopes().isPresent()) {
      form.set("scope", String.join(" ", config.getScopes().get()));
    }

    return request.sendForm(form);
  }

  @Override
  protected Token toToken(JsonObject json) {
    return Token.fromJwt(json.getString("access_token"));
  }
}

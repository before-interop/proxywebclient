package com.orange.proxywebclient.impl.method;

import com.orange.proxywebclient.AuthenticationMethodOauth2;
import com.orange.proxywebclient.Partner;
import com.orange.proxywebclient.ProxyAuthMethod;
import com.orange.proxywebclient.impl.ConfigPartnerAuth;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import java.time.Instant;
import java.util.Map;

@ProxyAuthMethod
public class AltitudeOsa extends AuthenticationMethodOauth2 {

  @RegisterForReflection
  public static class Config extends ConfigPartnerAuth {}

  @Override
  public void validate(Map<String, Object> config) {
    Partner.validateConfig(config, Config.class);
  }

  @Override
  protected Uni<HttpResponse<Buffer>> getTokenFromApi() {
    var config = getPartner().getConfig(Config.class);
    var request =
        getClientRequest(HttpMethod.GET, config.getTokenUrl().orElseThrow())
            .addQueryParam("login", config.getLogin())
            .addQueryParam("password", config.getPassword());

    return request.send();
  }

  @Override
  protected Token toToken(JsonObject json) {
    var expire = Instant.parse(json.getString("expire")).getEpochSecond();
    return new Token().setToken(json.getString("value")).setExp(expire);
  }
}

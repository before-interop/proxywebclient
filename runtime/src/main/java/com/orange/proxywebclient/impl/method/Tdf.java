package com.orange.proxywebclient.impl.method;

import com.orange.proxywebclient.AuthenticationMethodOauth2;
import com.orange.proxywebclient.Partner;
import com.orange.proxywebclient.ProxyAuthMethod;
import com.orange.proxywebclient.impl.ConfigPartnerAuth;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpRequest;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import java.time.Instant;
import java.util.Map;

@ProxyAuthMethod
public class Tdf extends AuthenticationMethodOauth2 {

  @Override
  public void validate(Map<String, Object> config) {
    Partner.validateConfig(config, ConfigPartnerAuth.class);
  }

  @Override
  protected Uni<HttpResponse<Buffer>> getTokenFromApi() {
    var config = getPartner().getConfig(ConfigPartnerAuth.class);
    var request = getClientRequest(HttpMethod.POST, config.getTokenUrl().orElseThrow());

    var body =
        JsonObject.of(
            "username", config.getLogin(),
            "password", config.getPassword());

    return request.sendJson(body);
  }

  @Override
  protected Token toToken(JsonObject json) {
    var now = Instant.now().getEpochSecond();

    return new Token()
        .setExp(now + json.getLong("expires_in") / 1000)
        .setToken(json.getString("access_token"));
  }

  @Override
  protected Uni<HttpRequest<Buffer>> getPreparedClientRequest(String uri) {
    return getToken()
        .onItem()
        .transformToUni(
            token ->
                super.getPreparedClientRequest(uri)
                    .onItem()
                    .transform(request -> request.putHeader("Authorization", token)));
  }
}

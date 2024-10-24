package com.orange.proxywebclient.impl.method;

import com.orange.proxywebclient.AuthenticationMethodBase;
import com.orange.proxywebclient.Partner;
import com.orange.proxywebclient.ProxyAuthMethod;
import com.orange.proxywebclient.impl.ConfigPartnerAuth;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.vertx.core.http.HttpMethod;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpRequest;
import java.util.Map;

@ProxyAuthMethod
public class Basic extends AuthenticationMethodBase {
  @RegisterForReflection
  public static class Config extends ConfigPartnerAuth {}

  @Override
  public void validate(Map<String, Object> config) {
    Partner.validateConfig(config, Config.class);
  }

  @Override
  protected HttpRequest<Buffer> getClientRequest(HttpMethod method, String uri) {
    var config = getPartner().getConfig(Config.class);
    return super.getClientRequest(method, uri)
        .basicAuthentication(config.getLogin(), config.getPassword());
  }
}

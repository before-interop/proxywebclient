package app.method;

import com.orange.proxywebclient.AuthenticationMethodBase;
import com.orange.proxywebclient.ProxyAuthMethod;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpRequest;

@ProxyAuthMethod
public class Custom extends AuthenticationMethodBase {

  @Override
  protected Uni<HttpRequest<Buffer>> getPreparedClientRequest(String uri) {
    return super.getPreparedClientRequest(uri)
        .onItem()
        .transform(
            req -> req.putHeader("X-DOUDOU", "doudou").putHeader("Authorization", "Basic doudou"));
  }
}

package app.method;

import com.orange.proxywebclient.ProxyAuthMethod;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpRequest;

@ProxyAuthMethod
@SuppressWarnings("java:S2176")
public class Basic extends com.orange.proxywebclient.impl.method.Basic {

  @Override
  protected Uni<HttpRequest<Buffer>> getPreparedClientRequest(String uri) {
    return super.getPreparedClientRequest(uri)
        .onItem()
        .transform(req -> req.putHeader("X-DOUDOU", "doudou"));
  }
}

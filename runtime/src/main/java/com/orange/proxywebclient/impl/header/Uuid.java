package com.orange.proxywebclient.impl.header;

import com.orange.proxywebclient.HeaderGenerator;
import com.orange.proxywebclient.Partner;
import io.quarkus.arc.Unremovable;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpRequest;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
@Unremovable
public class Uuid implements HeaderGenerator {

  @Override
  public void generate(String name, Partner.Header options, HttpRequest<Buffer> request) {
    if (!options.isOverwrite() && request.headers().contains(name)) {
      return;
    }

    request.putHeader(name, UUID.randomUUID().toString());
  }
}

package com.orange.proxywebclient.test.tool.wiremock;

import com.github.jknack.handlebars.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.helpers.HandlebarsHelper;
import io.vertx.core.json.JsonObject;
import java.io.IOException;
import java.time.Instant;
import java.util.Base64;

public class JwtHelper extends HandlebarsHelper<Object> {

  @Override
  public Object apply(Object context, Options options) throws IOException {
    var payload = new JsonObject(options.hash);
    var signature = "aSBsb3ZlIGRvdWRvdQ";
    var header = new JsonObject().put("alg", "RS512").put("typ", "JWT");
    ;

    var iat = Instant.now().getEpochSecond();
    var exp = iat + (int) options.hash("maxAge", 3600);

    payload.put("iat", iat).put("exp", exp).remove("maxAge");

    return toBase64(header) + "." + toBase64(payload) + "." + signature;
  }

  private String toBase64(JsonObject json) {
    var str = json.encode();

    return Base64.getEncoder().withoutPadding().encodeToString(str.getBytes());
  }
}

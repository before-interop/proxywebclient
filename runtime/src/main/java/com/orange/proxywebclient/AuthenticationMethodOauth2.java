package com.orange.proxywebclient;

import com.orange.proxywebclient.impl.ConfigPartnerAuth;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpRequest;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.regex.Pattern;
import org.jboss.logging.Logger;

public abstract class AuthenticationMethodOauth2 extends AuthenticationMethodBase {

  private static final Logger log = Logger.getLogger(AuthenticationMethodOauth2.class);

  private static final Pattern JW_PATTERN = Pattern.compile("^[^\\.]+\\.([^\\.]+).+");

  public static final String CACHE_NAME = "token";

  public static final int TOKEN_EXPIRATION_MARGIN = 60;

  @Inject Cache cache;

  protected abstract Uni<HttpResponse<Buffer>> getTokenFromApi();

  protected abstract Token toToken(JsonObject json);

  protected Uni<String> getToken() {
    return cache
        .get(getPartner(), CACHE_NAME)
        .onItem()
        .ifNull()
        .switchTo(
            () ->
                getTokenFromApi()
                    .onItem()
                    .invoke(v -> log.info("regenerate token"))
                    .map(HttpResponse::bodyAsJsonObject)
                    .map(this::toToken)
                    .onItem()
                    .transformToUni(t -> cache.put(getPartner(), CACHE_NAME, t)))
        .map(Token::new)
        .onItem()
        .transformToUni(
            t -> {
              if (isExpired(t)) {
                log.info("token expired");
                return cache
                    .remove(getPartner(), CACHE_NAME)
                    .onItem()
                    .transformToUni(v -> getToken());
              }

              return Uni.createFrom().item(t.getToken());
            });
  }

  @Override
  protected Uni<HttpRequest<Buffer>> getPreparedClientRequest(String uri) {
    return getToken()
        .onItem()
        .transformToUni(
            token ->
                super.getPreparedClientRequest(uri)
                    .onItem()
                    .transform(request -> request.putHeader("Authorization", "Bearer " + token)));
  }

  public static boolean isExpired(Token token) {
    var now = Instant.now().getEpochSecond();
    return token.getExp() - TOKEN_EXPIRATION_MARGIN < now;
  }

  public static class Token {
    private long iat;
    private long exp;
    private String token; // NOSONAR

    public Token() {}

    public Token(JsonObject json) {
      fromJsonObject(json);
    }

    public Token(String token) {
      JsonObject json = new JsonObject(token);
      fromJsonObject(json);
    }

    public long getIat() {
      return iat;
    }

    public Token setIat(long iss) {
      this.iat = iss;
      return this;
    }

    public long getExp() {
      return exp;
    }

    public Token setExp(long exp) {
      this.exp = exp;
      return this;
    }

    public String getToken() {
      return token;
    }

    public Token setToken(String token) {
      this.token = token;
      return this;
    }

    private Token fromJsonObject(JsonObject json) {
      this.iat = json.getLong("iat");
      this.exp = json.getLong("exp");
      this.token = json.getString("token"); // NOSONAR
      return this;
    }

    @Override
    public String toString() {
      return new JsonObject(
              Map.of(
                  "iat", iat,
                  "exp", exp,
                  "token", token))
          .toString();
    }

    public static final Token fromJwt(String jwt) {
      JsonObject json = parseJwtToken(jwt);
      return new Token().setIat(json.getLong("iat")).setExp(json.getLong("exp")).setToken(jwt);
    }

    public static final JsonObject parseJwtToken(String token) {
      var payload = JW_PATTERN.matcher(token).replaceAll("$1");
      var jsonStr = new String(Base64.getDecoder().decode(payload));
      return new JsonObject(jsonStr);
    }
  }

  @Override
  public void validate(Map<String, Object> config) {
    Partner.validateConfig(config, ConfigPartnerAuth.class);
    var conf = Partner.getConfig(config, ConfigPartnerAuth.class);
    if (conf.getTokenUrl().isEmpty()) {
      throw new IllegalArgumentException("tokenUrl is required");
    }
  }
}

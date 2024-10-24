package com.orange.proxywebclient;

import io.quarkus.arc.Arc;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.core.net.ProxyOptions;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.http.HttpServerRequest;
import io.vertx.mutiny.ext.web.client.HttpRequest;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import io.vertx.mutiny.ext.web.multipart.MultipartForm;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import java.io.File;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

/** Abstract class to implement an authentication method */
public abstract class AuthenticationMethodBase implements AuthenticationMethod {

  @Inject ProxyConfig proxyConfig;

  private static final Logger log = Logger.getLogger(AuthenticationMethodBase.class);

  private static final List<String> IGNORE_HEADERS =
      List.of(
          "host",
          "content-length",
          "transfer-encoding",
          "connection",
          "Authorization",
          "x-oapi-application-id");

  private HttpServerRequest request;

  private RoutingContext routingContext;

  private Partner partner;

  private WebClient webClient;

  private Buffer body;

  @Override
  public final Uni<Buffer> proxy(
      Partner partner, HttpServerRequest request, Buffer body, RoutingContext routingContext) {
    this.partner = partner;
    this.request = request;
    this.body = body;
    this.routingContext = routingContext;

    return send(rewrite(getUri()));
  }

  /** Tool method to retrieve the Vert.x instance */
  protected final Vertx getVertx() {
    return Arc.container().instance(Vertx.class).get();
  }

  /** Tool method to retrieve the {@link WebClient} instance */
  protected final WebClient getWebClient() {
    if (webClient == null) {
      webClient = WebClient.create(getVertx(), getWebClientOptions());
    }

    return webClient;
  }

  /** Tool method to retrieve the {@link Partner} information */
  protected final Partner getPartner() {
    return partner;
  }

  /** Tool method to retrieve the {@link HeaderGenerator.Loader} instance */
  protected final HeaderGenerator.Loader getHeaderGeneratorLoader() {
    return Arc.container().instance(HeaderGenerator.Loader.class).get();
  }

  /**
   * Tool method to retrieve the application configuration property {@link
   * io.quarkus.runtime.TlsConfig#trustAll}
   */
  protected final boolean getQuarkusTlsTrustAll() {
    return ConfigProvider.getConfig()
        .getOptionalValue("quarkus.tls.trust-all", Boolean.class)
        .orElse(false);
  }

  /** Tool method to initialize the trust store */
  private void trustStore(WebClientOptions opts) {
    if (partner.getSsl().getTrustStore().isEmpty() || isTrustAll()) {
      return;
    }

    var conf = partner.getSsl().getTrustStore().get(); // NOSONAR
    if (new File(conf.getValue()).exists()) {
      if (conf.getValue().endsWith(".jks")) {
        var jks =
            new JksOptions()
                .setPath(conf.getValue())
                .setPassword(
                    conf.getPassword()
                        .orElseThrow(
                            () ->
                                new Proxy.NotImplementedException(
                                    "Missing password for trust store")));
        opts.setTrustStoreOptions(jks);
      } else {
        opts.setPemTrustOptions(new PemTrustOptions().addCertPath(conf.getValue()));
      }
    } else {
      // decode base64 to string
      var pem = new String(Base64.getDecoder().decode(conf.getValue()));
      // convert base64 string to buffer
      var buffer = Buffer.buffer(pem);

      opts.setPemTrustOptions(new PemTrustOptions().addCertValue(buffer.getDelegate()));
    }
  }

  /** Tool method to initialize the key store */
  private void keyStore(WebClientOptions opts) {
    var confSSL = partner.getSsl();
    if (confSSL.getKeyStore().isPresent()) {
      var confKeyStore = confSSL.getKeyStore().get();
      if (confKeyStore.getValue().endsWith(".jks")) {
        var jks =
            new JksOptions()
                .setPath(confKeyStore.getValue())
                .setPassword(
                    confKeyStore
                        .getPassword()
                        .orElseThrow(
                            () ->
                                new Proxy.NotImplementedException(
                                    "Missing password for key store")));
        opts.setKeyStoreOptions(jks);
      } else {
        throw new Proxy.NotImplementedException("Format Keystore must be \".jks\"");
      }
    } else if (confSSL.getX509PemFile().isPresent()) {
      var x509 = confSSL.getX509PemFile().get();
      opts.setPemKeyCertOptions(
          new PemKeyCertOptions()
              .addKeyPath(
                  x509.getKeyPem()
                      .orElseThrow(
                          () -> new Proxy.NotImplementedException("X509PemFile.keyPem empty")))
              .addCertPath(
                  x509.getCertPem()
                      .orElseThrow(
                          () -> new Proxy.NotImplementedException("X509PemFile.certPem empty"))));
    } else if (confSSL.getX509Pem().isPresent()) {
      var x509 = confSSL.getX509Pem().get();
      // decode base64 to string
      var pemCert =
          new String(
              Base64.getDecoder()
                  .decode(
                      x509.getCertPem()
                          .orElseThrow(
                              () -> new Proxy.NotImplementedException("X509Pem.certPem empty"))));
      // convert base64 string to buffer
      var bufferCert = Buffer.buffer(pemCert);
      // decode base64 to string
      var pemKey =
          new String(
              Base64.getDecoder()
                  .decode(
                      x509.getKeyPem()
                          .orElseThrow(
                              () -> new Proxy.NotImplementedException("X509Pem.keyPem empty"))));
      ;
      // convert base64 string to buffer
      var bufferKey = Buffer.buffer(pemKey);

      opts.setPemKeyCertOptions(
          new PemKeyCertOptions()
              .addKeyValue(bufferKey.getDelegate())
              .addCertValue(bufferCert.getDelegate()));
    }
  }

  /** Get the Web Client options */
  protected WebClientOptions getWebClientOptions() {
    var wco =
        new WebClientOptions()
            .setSsl(isSSl())
            .setFollowRedirects(isFollowRedirects())
            .setTrustAll(isTrustAll())
            .setVerifyHost(isVerifyHost());

    trustStore(wco);
    keyStore(wco);

    return wco;
  }

  /** Rewrite the URL if needed */
  public String rewrite(String url) {
    var newUrl = url;

    for (var config : partner.getUrlRewrites()) {
      var rewriteUrl = newUrl.replaceAll(config.getMatches(), config.getReplace());

      if (config.isStop() && !rewriteUrl.equals(newUrl)) {
        return rewriteUrl;
      }

      newUrl = rewriteUrl;
    }

    return newUrl;
  }

  /**
   * Get the path from the request
   *
   * @return the path
   */
  protected String getUri() {
    var uri = getBaseUrl();
    var path = request.getParam("path", "");
    if (!uri.endsWith("/") && !path.startsWith("/")) {
      uri += "/";
    } else if (uri.endsWith("/") && path.startsWith("/")) {
      path = path.replaceFirst("\\/", "");
    }
    uri += path;

    var query = request.query();

    return query == null ? uri : uri + "?" + query;
  }

  /**
   * Add extra headers to the request
   *
   * <p>In the default implementation, write order is:
   *
   * <ul>
   *   <li>Extra headers from the configuration
   *   <li>Extra headers from the partner configuration
   * </ul>
   */
  protected void addExtraHeaders(HttpRequest<Buffer> request) {
    proxyConfig.header.extra.forEach(
        (k, v) -> {
          var headerOption = new Partner.Header(v);
          getHeaderGeneratorLoader()
              .get(headerOption.getGenerator())
              .generate(k, headerOption, request, this);
        });

    getPartner()
        .getExtraHeaders()
        .forEach(
            (k, v) ->
                getHeaderGeneratorLoader().get(v.getGenerator()).generate(k, v, request, this));
  }

  /** Add the header <code>X-Request-ID</code> to the request if it is not already present. */
  protected void setRequestId() {
    var requestId = request.headers().get("X-Request-ID");

    if (requestId == null) {
      requestId = UUID.randomUUID().toString();
      request.headers().add("X-Request-ID", requestId);
    }

    MDC.put("requestId", requestId);
  }

  /**
   * Check if the method is allowed to follow redirects
   *
   * @return true if the method is allowed to follow redirects
   */
  public boolean isFollowRedirects() {
    return partner.isFollowRedirects();
  }

  /** True if the partner certificate should be trusted even if it is not valid */
  protected boolean isTrustAll() {
    if (getQuarkusTlsTrustAll()) {
      return true;
    }

    return partner.getSsl().isTrustAll();
  }

  /** Check if the SSL validator verify the host */
  protected boolean isSSl() {
    return partner.getSsl().isSsl();
  }

  /** Check if the SSL validator verify the host */
  protected boolean isVerifyHost() {
    if (getQuarkusTlsTrustAll() || isTrustAll()) {
      return false;
    }

    return partner.getSsl().isVerifyHost();
  }

  /** Return the base url of the partner or throw an exception if it is not configured */
  protected String getBaseUrl() {
    return partner
        .getBaseUrl()
        .orElseThrow(
            () ->
                new Proxy.NotImplementedException(
                    "No base url configured for partner " + partner.getClientId()));
  }

  /** Get the partner configuration from the clientId */
  @SuppressWarnings("java:S3655")
  protected ProxyOptions getProxyOptions() {
    if (!partner.isUseProxy()) {
      return null;
    }

    var proxyConf = partner.getProxy().orElse(new Partner.Proxy(proxyConfig.proxy));
    var proxy = new ProxyOptions();

    if (proxyConf != null) {
      proxy.setHost(proxyConf.getHost());
      proxy.setPort(proxyConf.getPort());

      if (proxyConf.getUsername().isPresent() && proxyConf.getPassword().isPresent()) {
        proxy.setUsername(proxyConf.getUsername().get());
        proxy.setPassword(proxyConf.getPassword().get());
      }
    }

    return proxy;
  }

  /** Get an HTTP client request object */
  protected HttpRequest<Buffer> getClientRequest(HttpMethod method, String uri) {
    return getWebClient()
        .requestAbs(method, uri)
        .followRedirects(isFollowRedirects())
        .proxy(getProxyOptions());
  }

  /** Send the response to the client */
  protected Uni<Buffer> send(String uri) {
    return call(uri)
        .map(
            resp -> {
              var response = request.response();
              response.setStatusCode(resp.statusCode());
              resp.headers().forEach(h -> response.putHeader(h.getKey(), h.getValue()));
              return Optional.ofNullable(resp.body()).orElse(Buffer.buffer());
            });
  }

  protected Uni<HttpRequest<Buffer>> getPreparedClientRequest(String uri) {
    var r = getClientRequest(request.method(), uri);
    var host = uri.replaceAll("https?://([^/]+).*", "$1");

    // generate a request id if it doesn't exist
    setRequestId();

    // copy headers
    request
        .headers()
        .forEach(
            h -> {
              if (!IGNORE_HEADERS.contains(h.getKey().toLowerCase())) {
                r.putHeader(h.getKey(), h.getValue());
              }
            });

    // overwrite the `Host` header by the host of the uri
    // this hack prevents some servers to return a 40X error
    // if you need to override the host header, use the `extra` configuration
    // and set the option `overwrite` to true
    r.putHeader("Host", host);

    // add extra headers
    addExtraHeaders(r);

    return Uni.createFrom().item(r);
  }

  /** Call the partner */
  protected Uni<HttpResponse<Buffer>> call(String uri) {
    MDC.put("uri", uri);
    MDC.put("method", request.method().name());

    if (request.headers().get(HttpHeaders.CONTENT_TYPE) != null
        && request
            .headers()
            .get(HttpHeaders.CONTENT_TYPE)
            .contains(MediaType.MULTIPART_FORM_DATA)) {

      return sendMultipartForm(uri);
    } else {
      return getPreparedClientRequest(uri)
          .onItem()
          .transformToUni(r -> r.sendBuffer(body))
          .onItem()
          .invoke(resp -> log.info("Response from partner: " + resp.statusCode()));
    }
  }

  private Uni<HttpResponse<Buffer>> sendMultipartForm(String uri) {
    MultipartForm form = MultipartForm.create();

    // Iterate over the uploaded files and add them to the multipart form
    for (FileUpload fileUpload : routingContext.fileUploads()) {
      form.binaryFileUpload(
          fileUpload.name(),
          fileUpload.fileName(),
          fileUpload.uploadedFileName(),
          fileUpload.contentType());
    }

    // Add the other form parameters
    routingContext
        .request()
        .formAttributes()
        .forEach(entry -> form.attribute(entry.getKey(), entry.getValue()));

    // Send the request with the multipart form
    return getPreparedClientRequest(uri)
        .onItem()
        .transformToUni(
            request -> {
              request.headers().remove(HttpHeaders.CONTENT_TYPE);
              return request.sendMultipartForm(form);
            })
        .onItem()
        .invoke(resp -> log.info("Response from partner: " + resp.statusCode()));
  }
}

package com.orange.proxywebclient;

import io.quarkus.arc.Arc;
import io.smallrye.mutiny.Uni;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Partner {
  @NotBlank private String clientId;
  @NotBlank private String method;
  private Optional<String> baseUrl = Optional.empty();
  private boolean useProxy = false;
  private boolean followRedirects = true;
  private Retry retry = new Retry();
  private SSL ssl = new SSL();
  private Optional<Proxy> proxy = Optional.empty();
  private Map<String, Header> extraHeaders = new HashMap<>();
  private List<@Valid Rewrite> urlRewrites = new ArrayList<>();
  private Config config = new Config();

  /** Get the client id */
  public String getClientId() {
    return clientId;
  }

  /** Set the client id This is the client id used to identify the partner */
  public Partner setClientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  /** Get the base url */
  public Optional<String> getBaseUrl() {
    return baseUrl;
  }

  /** Set the base url Most of the time, this is the hostname used by the partner */
  public Partner setBaseUrl(Optional<String> baseUrl) {
    this.baseUrl = baseUrl;
    return this;
  }

  /** Get the authentication method */
  public String getMethod() {
    return method;
  }

  /** Set the authentication method This is the authentication method used by the partner */
  public Partner setMethod(String method) {
    this.method = method;
    return this;
  }

  /** Get the retry configuration */
  public Retry getRetry() {
    return retry;
  }

  /** Set the retry configuration */
  public Partner setRetry(Retry retry) {
    this.retry = retry;
    return this;
  }

  /** Get the SSL configuration */
  public SSL getSsl() {
    return ssl;
  }

  /** Set the SSL configuration */
  public Partner setSsl(SSL ssl) {
    this.ssl = ssl;
    return this;
  }

  /** Get the proxy configuration */
  public Optional<Proxy> getProxy() {
    return proxy;
  }

  /** Set the proxy configuration */
  public Partner setProxy(Optional<Proxy> proxy) {
    this.proxy = proxy;
    return this;
  }

  /** True if the partner need a proxy to access be accessed */
  public boolean isUseProxy() {
    return useProxy;
  }

  /** Set it to TRUE if the partner need a proxy to access be accessed */
  public Partner setUseProxy(boolean useProxy) {
    this.useProxy = useProxy;
    return this;
  }

  /** True if the partner should follow redirects */
  public boolean isFollowRedirects() {
    return followRedirects;
  }

  /** Set it to TRUE if the partner should follow redirects Default is TRUE */
  public Partner setFollowRedirects(boolean followRedirects) {
    this.followRedirects = followRedirects;
    return this;
  }

  /** Get the url rewrites config */
  public List<Rewrite> getUrlRewrites() {
    return urlRewrites;
  }

  /** Set the url rewrites config */
  public Partner setUrlRewrites(@Valid List<Rewrite> urlRewrites) {
    this.urlRewrites = urlRewrites;
    return this;
  }

  /** Get the extra headers */
  public Map<String, Header> getExtraHeaders() {
    return extraHeaders;
  }

  /** Set the extra headers */
  public Partner setExtraHeaders(Map<String, Header> extraHeaders) {
    this.extraHeaders = extraHeaders;
    return this;
  }

  /** Get the custom configuration */
  public Config getConfig() {
    return config;
  }

  /** Get the custom configuration as a specific type */
  public <T> T getConfig(Class<T> clazz) {
    return getConfig(config, clazz);
  }

  /** Get the custom configuration as a specific type */
  public static final <T> T getConfig(Map<String, Object> config, Class<T> clazz) {
    var parser = Arc.container().instance(ConfigConverter.class).get();

    return parser.cast(config, clazz);
  }

  /** Get the custom configuration as a specific type */
  public static final <T> void validateConfig(Map<String, Object> config, Class<T> clazz) {
    var customConfig = getConfig(config, clazz);

    var validator = Arc.container().instance(Validator.class).get();
    var violationReport = validator.validate(customConfig);

    if (!violationReport.isEmpty()) {
      throw new ConstraintViolationException(violationReport);
    }
  }

  /** Set the custom configuration This is the custom configuration used by the partner */
  public Partner setConfig(Config config) {
    this.config = config;
    return this;
  }

  public static class Config extends HashMap<String, Object> {}

  public static class Proxy {
    private String host = "localhost";
    private int port = 3128;
    private Optional<String> username = Optional.empty();
    private Optional<String> password = Optional.empty();

    public Proxy() {}

    public Proxy(ProxyConfig.Proxy proxy) {
      this.host = proxy.host;
      this.port = proxy.port;
      this.username = proxy.username;
      this.password = proxy.password;
    }

    /** Get the proxy host */
    public String getHost() {
      return host;
    }

    /** Set the proxy host */
    public Proxy setHost(String host) {
      this.host = host;
      return this;
    }

    /** Get the proxy port */
    public int getPort() {
      return port;
    }

    /** Set the proxy port */
    public Proxy setPort(int port) {
      this.port = port;
      return this;
    }

    /** Get the proxy username */
    public Optional<String> getUsername() {
      return username;
    }

    /** Set the proxy username */
    public Proxy setUsername(Optional<String> username) {
      this.username = username;
      return this;
    }

    /** Get the proxy password */
    public Optional<String> getPassword() {
      return password;
    }

    /** Set the proxy password */
    public Proxy setPassword(Optional<String> password) {
      this.password = password;
      return this;
    }
  }

  public static class SSL {
    private boolean ssl = true;
    private boolean trustAll = false;
    private boolean verifyHost = true;
    private Optional<Store> trustStore = Optional.empty();
    private Optional<Store> keyStore = Optional.empty();
    private Optional<X509Certificat> x509PemFile = Optional.empty();
    private Optional<X509Certificat> x509Pem = Optional.empty();

    /**
     * ssl is TRUE by default.
     *
     * @return
     */
    public boolean isSsl() {
      return ssl;
    }

    /** set to FALSE to desabled SSL mode. */
    public void setSsl(boolean ssl) {
      this.ssl = ssl;
    }

    /** True if the partner certificate should be trusted even if it is not valid */
    public boolean isTrustAll() {
      return trustAll;
    }

    /** Set it to TRUE if the partner certificate should be trusted even if it is not valid */
    public SSL setTrustAll(boolean trustAll) {
      this.trustAll = trustAll;
      return this;
    }

    /**
     * FALSE if the partner certificate should be trusted even if the host does not match the
     * certificate
     */
    public boolean isVerifyHost() {
      return verifyHost;
    }

    /**
     * Set it to FALSE if the partner certificate should be trusted even if the host does not match
     * the certificate
     */
    public SSL setVerifyHost(boolean verifyHost) {
      this.verifyHost = verifyHost;
      return this;
    }

    /** Get the trust store configuration */
    public Optional<Store> getTrustStore() {
      return trustStore;
    }

    /** Set the trust store configuration */
    public SSL setTrustStore(Optional<Store> trustStore) {
      this.trustStore = trustStore;
      return this;
    }

    /** Get the key store configuration */
    public Optional<Store> getKeyStore() {
      return keyStore;
    }

    /** Set the key store configuration */
    public SSL setKeyStore(Optional<Store> keyStore) {
      this.keyStore = keyStore;
      return this;
    }

    public Optional<X509Certificat> getX509PemFile() {
      return x509PemFile;
    }

    public void setX509PemFile(Optional<X509Certificat> x509PemFile) {
      this.x509PemFile = x509PemFile;
    }

    public Optional<X509Certificat> getX509Pem() {
      return x509Pem;
    }

    public void setX509Pem(Optional<X509Certificat> x509Pem) {
      this.x509Pem = x509Pem;
    }

    public static class X509Certificat {
      private Optional<String> keyPem = Optional.empty();
      private Optional<String> certPem = Optional.empty();

      /** get the key pem configuration */
      public Optional<String> getKeyPem() {
        return keyPem;
      }

      /** Set the key pem configuration */
      public void setKeyPem(Optional<String> keyPem) {
        this.keyPem = keyPem;
      }

      /** get the cert pem configuration */
      public Optional<String> getCertPem() {
        return certPem;
      }

      /** set the cert pem configuration */
      public void setCertPem(Optional<String> certPem) {
        this.certPem = certPem;
      }
    }

    public static class Store {
      private String value;
      private Optional<String> password = Optional.empty();

      /**
       * Get the path to the store
       *
       * <p>can be a path to a PEM file or a path to a JKS file or a base64 encoded PEM file
       */
      public String getValue() {
        return value;
      }

      /**
       * Set the path to the store
       *
       * <p>can be a path to a PEM file or a path to a JKS file or a base64 encoded PEM file
       */
      public Store setValue(String value) {
        this.value = value;
        return this;
      }

      /** Get the password to the store */
      public Optional<String> getPassword() {
        return password;
      }

      /** Set the password to the store */
      public Store setPassword(Optional<String> password) {
        this.password = password;
        return this;
      }
    }
  }

  public static class Rewrite {
    @NotBlank private String matches;

    @NotBlank private String replace;

    private boolean stop = false;

    public String getMatches() {
      return matches;
    }

    public Rewrite setMatches(String matches) {
      this.matches = matches;
      return this;
    }

    public String getReplace() {
      return replace;
    }

    public Rewrite setReplace(String replace) {
      this.replace = replace;
      return this;
    }

    public boolean isStop() {
      return stop;
    }

    public Rewrite setStop(boolean stop) {
      this.stop = stop;
      return this;
    }
  }

  public static class Header {
    private String generator = "Copy";
    private Optional<String> value = Optional.empty();
    private boolean overwrite = false;

    public Header() {}

    public Header(ProxyConfig.Header.Config config) {
      this.generator = config.generator;
      this.value = config.value;
      this.overwrite = config.overwrite;
    }

    /** Get the header value */
    public Optional<String> getValue() {
      return value;
    }

    /** Set the header value */
    public Header setValue(Optional<String> value) {
      this.value = value;
      return this;
    }

    /** Get the header generator */
    public String getGenerator() {
      return generator;
    }

    /** Set the header generator */
    public Header setGenerator(String generator) {
      this.generator = generator;
      return this;
    }

    /** True if the header should be overwritten */
    public boolean isOverwrite() {
      return overwrite;
    }

    /** Set it to TRUE if the header should be overwritten */
    public Header setOverwrite(boolean overwrite) {
      this.overwrite = overwrite;
      return this;
    }
  }

  public static class Retry {
    private boolean enabled = false;
    private int max = 3;
    private long delay = Duration.ofSeconds(3).toMillis();
    private Double jitter = .2;
    private Condition condition = new Condition();

    /*
     * Is retry enabled
     */
    public boolean isEnabled() {
      return enabled;
    }

    /*
     * Enable or disable retry
     *
     * default: false
     */
    public Retry setEnabled(boolean enabled) {
      this.enabled = enabled;
      return this;
    }

    /*
     * Get the maximum number of retries
     */
    public int getMax() {
      return max;
    }

    /*
     * Set the maximum number of retries
     *
     * default: 3
     */
    public Retry setMax(int max) {
      this.max = max;
      return this;
    }

    /*
     * Get the delay between retries
     */
    public long getDelay() {
      return delay;
    }

    /*
     * Set the delay between retries
     *
     * default: 3s
     */
    public Retry setDelay(long delay) {
      this.delay = delay;
      return this;
    }

    /*
     * Get the jitter
     */
    public Double getJitter() {
      return jitter;
    }

    /*
     * Set the jitter
     *
     * default: .2
     */
    public Retry setJitter(Double jitter) {
      this.jitter = jitter;
      return this;
    }

    /*
     * Get the condition
     */
    public Condition getCondition() {
      return condition;
    }

    /*
     * Set the condition
     *
     * default: 500,502,503,504
     */
    public Retry setCondition(Condition condition) {
      this.condition = condition;
      return this;
    }

    public static class Condition {
      private String status = "500,502,503,504";

      /*
       * Get the status codes that should trigger a retry
       */
      public Stream<Integer> getStatus() {
        return Arrays.asList(status.split(",")).stream().map(Integer::parseInt);
      }

      /*
       * Set the status codes that should trigger a retry
       *
       * default: 500,502,503,504
       */
      public Condition setStatus(String... status) {
        var statusList = new ArrayList<String>();
        for (var s : status) {
          Arrays.asList(s.split(",")).stream()
              .map(String::trim)
              .map(Integer::parseInt)
              .map(String::valueOf)
              .forEach(statusList::add);
        }
        this.status = statusList.stream().collect(Collectors.joining(","));

        return this;
      }
    }
  }

  public static class ConfigConverterException extends RuntimeException {
    public ConfigConverterException(Throwable cause) {
      super(cause);
    }
  }

  public static class PartnerNotFoundException extends RuntimeException {
    public PartnerNotFoundException(String clientId) {
      super(clientId);
    }
  }

  public interface ConfigConverter {

    /*
     * Convert the config to a specific type
     */
    <T> T cast(Map<String, Object> config, Class<T> clazz);
  }

  public interface Service {

    /**
     * Creates or updates a partner.
     *
     * @param partner the partner to create or update
     * @return the created or updated partner
     */
    Uni<Partner> upsert(Partner partner);

    /**
     * Deletes an existing partner.
     *
     * @param clientId the partner to delete
     */
    Uni<Void> delete(String clientId);

    /**
     * Gets an existing partner.
     *
     * @param clientId the partner id
     * @return the partner
     */
    Uni<Partner> find(String clientId);

    /**
     * Gets all existing partners.
     *
     * @return the partners
     */
    Uni<List<Partner>> list();
  }
}

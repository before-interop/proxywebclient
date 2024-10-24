package com.orange.proxywebclient;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@ConfigRoot(prefix = "proxy", name = "", phase = ConfigPhase.RUN_TIME)
@SuppressWarnings("java:S1104")
public final class ProxyConfig {

  /** Specifies if the application is running in production mode. */
  @ConfigItem(defaultValue = "production")
  public boolean environment;

  /**
   * The packages to scan for authentication methods.
   *
   * <p>Multiple packages can be specified using a comma separated list.
   */
  public Optional<String> packages;

  /** Configuration for the default proxy. */
  public Proxy proxy = new Proxy();

  /** Headers configuration. */
  public Header header;

  /** The timeout to use when connecting to the partner. */
  @ConfigItem(defaultValue = "PT30S")
  public Duration timeout;

  @ConfigGroup
  public static class SSL {

    /** Specifies if the SSL certificate should be trusted. */
    @ConfigItem(defaultValue = "false")
    public boolean trustAll;

    /** Specifies if the SSL certificate should be verified the hostname. */
    @ConfigItem(defaultValue = "true")
    public boolean verifyHost;
  }

  @ConfigGroup
  public static class Header {

    /**
     * The package to scan for a generator. Multiple packages can be specified using a comma
     * separated list.
     */
    public Optional<String> packages;

    /** Extra headers to add to the request. */
    public Map<String, Config> extra;

    @ConfigGroup
    public static class Config {

      /** The generator to use to generate the value of the header. */
      @ConfigItem(defaultValue = "Copy")
      public String generator;

      /** The value of the header. */
      public Optional<String> value;

      /** Specifies if the header should be overwritten if it already exists. */
      @ConfigItem(defaultValue = "false")
      public boolean overwrite;
    }
  }

  @ConfigGroup
  public static class Proxy {

    /** The host of the proxy. */
    @ConfigItem(defaultValue = "localhost")
    public String host;

    /** The port of the proxy. */
    @ConfigItem(defaultValue = "3128")
    public int port;

    /** The username to use to connect to the proxy. */
    public Optional<String> username;

    /** The password to use to connect to the proxy. */
    public Optional<String> password;
  }
}

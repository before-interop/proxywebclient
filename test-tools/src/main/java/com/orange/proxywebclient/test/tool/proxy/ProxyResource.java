package com.orange.proxywebclient.test.tool.proxy;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Map;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

public class ProxyResource implements QuarkusTestResourceLifecycleManager {

  private static final String IMAGE = "ubuntu/squid:latest";
  private static final int PORT = 3128;

  private GenericContainer<?> server;

  @Override
  public Map<String, String> start() {

    var waitingStrategy = new HttpWaitStrategy().forPort(PORT).forStatusCode(400);

    server = new GenericContainer<>(IMAGE).withExposedPorts(PORT).waitingFor(waitingStrategy);

    server.start();

    var portMapped = server.getMappedPort(PORT);

    System.err.println("Proxy started on port " + portMapped);

    return Map.of(
        "proxy.proxy.port", String.valueOf(portMapped),
        "proxy.proxy.host", server.getHost());
  }

  @Override
  public void stop() {
    if (server != null) {
      server.stop();
    }
  }

  @Override
  public void inject(TestInjector injector) {
    injector.injectIntoFields(
        server,
        new TestInjector.AnnotatedAndMatchesType(ProxyServer.class, GenericContainer.class));
  }
}

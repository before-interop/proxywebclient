package com.orange.proxywebclient.test.tool.wiremock;

import com.github.jknack.handlebars.Helper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Map;

public class WiremockResource implements QuarkusTestResourceLifecycleManager {

  WireMockServer server;

  @Override
  public Map<String, String> start() {
    Map<String, Helper<?>> helpers = Map.of("jwt", new JwtHelper());

    var config =
        WireMockConfiguration.options()
            .dynamicPort()
            // .usingFilesUnderClasspath("classpath*:")
            .extensions(new ResponseTemplateTransformer(false, helpers));

    server = new WireMockServer(config);
    server.start();

    return Map.of();
  }

  @Override
  public void stop() {
    server.stop();
  }

  @Override
  public void inject(TestInjector injector) {
    injector.injectIntoFields(
        server, new TestInjector.AnnotatedAndMatchesType(WireMock.class, WireMockServer.class));
  }
}

package com.orange.proxywebclient.impl;

import static io.restassured.RestAssured.given;

import com.orange.proxywebclient.Partner;
import com.orange.proxywebclient.test.tool.proxy.ProxyResource;
import com.orange.proxywebclient.test.tool.proxy.ProxyServer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.net.UnknownHostException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

@QuarkusTest
@QuarkusTestResource(ProxyResource.class)
public class ProxyTest {

  private static final String baseUrl = "https://swapi.dev/api/people/1";

  @ProxyServer GenericContainer<?> proxy;

  @Test
  public void testProxy() throws UnknownHostException {
    var partner =
        new Partner()
            .setBaseUrl(Optional.of(baseUrl))
            .setClientId("swapi")
            .setMethod("None")
            .setUseProxy(true);

    given().body(partner).when().put("/partners").then().statusCode(200);

    given().when().get("/proxy/swapi").then().statusCode(200);
  }

  @Test
  public void testPartnerConfig() throws UnknownHostException {
    var proxyConfig =
        new Partner.Proxy().setHost(proxy.getHost()).setPort(proxy.getMappedPort(3128));

    var partner =
        new Partner()
            .setBaseUrl(Optional.of(baseUrl))
            .setClientId("swapi")
            .setMethod("None")
            .setUseProxy(true)
            .setProxy(Optional.of(proxyConfig));

    given().body(partner).when().put("/partners").then().statusCode(200);

    given().when().get("/proxy/swapi").then().statusCode(200);
  }
}

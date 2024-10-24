package com.orange.proxywebclient.impl;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.orange.proxywebclient.Partner;
import com.orange.proxywebclient.test.tool.wiremock.WireMock;
import com.orange.proxywebclient.test.tool.wiremock.WiremockResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.util.Optional;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@QuarkusTestResource(WiremockResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BasicTest {

  @WireMock WireMockServer server;

  @Test
  @Order(1)
  public void testValidation() {
    var partner =
        new Partner()
            .setBaseUrl(Optional.of(server.baseUrl()))
            .setClientId("basic")
            .setMethod("Basic");

    given().body(partner).when().put("/partners").then().statusCode(400);
  }

  @Test
  @Order(2)
  public void testPartnerImplementation() {
    var config = new Partner.Config();
    config.put("login", "username");
    config.put("password", "password");

    var partner =
        new Partner()
            .setBaseUrl(Optional.of(server.baseUrl()))
            .setClientId("basic")
            .setMethod("Basic")
            .setConfig(config);

    given().body(partner).when().put("/partners").then().statusCode(200);

    given()
        .when()
        .get("/proxy/basic/api/basic")
        .then()
        .statusCode(200)
        .body(is("OK for Basic method"));
  }
}

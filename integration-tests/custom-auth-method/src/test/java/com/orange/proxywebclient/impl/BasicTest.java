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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@QuarkusTestResource(WiremockResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BasicTest {

  @WireMock WireMockServer server;

  @Test
  public void testPartnerImplementationOverride() {
    var config = new Partner.Config();
    config.put("login", "username");
    config.put("password", "password");

    var partner =
        new Partner()
            .setBaseUrl(Optional.of(server.baseUrl()))
            .setClientId("basicOverride")
            .setMethod("Basic");

    given().body(partner).when().put("/partners").then().statusCode(400);

    partner.setConfig(config);

    given().body(partner).when().put("/partners").then().statusCode(200);

    given()
        .when()
        .get("/proxy/basicOverride/api/basic")
        .then()
        .statusCode(200)
        .body(is("OK for Basic method"));
  }

  @Test
  public void testPartnerImplementationPackage() {
    var config = new Partner.Config();
    config.put("login", "username");
    config.put("password", "password");

    var partner =
        new Partner()
            .setBaseUrl(Optional.of(server.baseUrl()))
            .setClientId("basicPackage")
            .setMethod("app.method.Basic")
            .setConfig(config);

    given().body(partner).when().put("/partners").then().statusCode(200);

    given()
        .when()
        .get("/proxy/basicPackage/api/basic")
        .then()
        .statusCode(200)
        .body(is("OK for Basic method"));
  }

  @Test
  public void testPartnerNoneImplementation() {
    var partner =
        new Partner()
            .setBaseUrl(Optional.of(server.baseUrl()))
            .setClientId("custom")
            .setMethod("Custom");

    given().body(partner).when().put("/partners").then().statusCode(200);

    given()
        .when()
        .get("/proxy/custom/api/basic")
        .then()
        .statusCode(200)
        .body(is("OK for Basic method"));
  }
}

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
public class CustomPathTest {

  @WireMock WireMockServer server;

  @Test
  @Order(1)
  public void testCustomPathProxyBaseUrdEndWithoutSlash() {

    var partner =
        new Partner()
            .setBaseUrl(Optional.of(server.baseUrl()))
            .setClientId("baseUriWithoutSlash")
            .setMethod("None");

    given().body(partner).when().put("/partners").then().statusCode(200);

    given()
        .when()
        .get("/api/baseUriWithoutSlash/test/none")
        .then()
        .statusCode(200)
        .body(is("OK for None method"));
  }

  @Test
  @Order(2)
  public void testCustomPathProxyBaseUrdEndWithSlash() {

    var partner =
        new Partner()
            .setBaseUrl(Optional.of(server.baseUrl() + "/"))
            .setClientId("baseUriWithSlash")
            .setMethod("None");

    given().body(partner).when().put("/partners").then().statusCode(200);

    given()
        .when()
        .get("/api/baseUriWithSlash/test/none")
        .then()
        .statusCode(200)
        .body(is("OK for None method"));
  }
}

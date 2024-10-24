package com.orange.proxywebclient.impl;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.orange.proxywebclient.Partner;
import com.orange.proxywebclient.test.tool.wiremock.WireMock;
import com.orange.proxywebclient.test.tool.wiremock.WiremockResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@QuarkusTestResource(WiremockResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GatapeTest {

  @WireMock WireMockServer server;

  @Test
  @Order(1)
  public void testValidation() {
    var partner =
        new Partner()
            .setBaseUrl(Optional.of(server.baseUrl()))
            .setClientId("gatape")
            .setMethod("Gatape");

    given().body(partner).when().put("/partners").then().statusCode(400);
  }

  @Test
  @Order(2)
  public void testPartnerImplementation() {
    var config = new Partner.Config();
    config.put("login", "myClientId");
    config.put("password", "myClientSecret");
    config.put("tokenUrl", server.baseUrl() + "/gatape/token");
    config.put("scopes", List.of("api-prd:photo"));

    var partner =
        new Partner()
            .setBaseUrl(Optional.of(server.baseUrl()))
            .setClientId("gatape")
            .setMethod("Gatape")
            .setConfig(config);

    given().body(partner).when().put("/partners").then().statusCode(200);

    given()
        .when()
        .get("/proxy/gatape/gatape-api")
        .then()
        .statusCode(200)
        .body(is("OK for gatape method"));

    given()
        .body(JsonObject.of("name", "doudou").encode())
        .when()
        .post("/proxy/gatape/gatape-api")
        .then()
        .statusCode(201);
  }

  @Test
  @Order(3)
  public void testPartnerImplementationWithAllScopes() {
    var config = new Partner.Config();
    config.put("login", "myClientId");
    config.put("password", "myClientSecret");
    config.put("tokenUrl", server.baseUrl() + "/gatape/token");

    var partner =
        new Partner()
            .setBaseUrl(Optional.of(server.baseUrl()))
            .setClientId("gatape")
            .setMethod("Gatape")
            .setConfig(config);

    given().body(partner).when().put("/partners").then().statusCode(200);

    given()
        .when()
        .get("/proxy/gatape/gatape-api")
        .then()
        .statusCode(200)
        .body(is("with All scope"));
  }
}

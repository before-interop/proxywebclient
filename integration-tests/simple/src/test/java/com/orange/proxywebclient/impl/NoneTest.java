package com.orange.proxywebclient.impl;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.orange.proxywebclient.Partner;
import com.orange.proxywebclient.test.tool.wiremock.WireMock;
import com.orange.proxywebclient.test.tool.wiremock.WiremockResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@QuarkusTestResource(WiremockResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NoneTest {

  @WireMock WireMockServer server;

  @Test
  @Order(1)
  public void testUnimplementedPartner() {
    given().when().get("/proxy/unimplemented").then().statusCode(501);
  }

  @Test
  @Order(1)
  public void testValidation() {
    var partner = new Partner().setBaseUrl(Optional.of(server.baseUrl() + "/"));

    given()
        .body(partner)
        .when()
        .put("/partners")
        .then()
        .statusCode(400)
        .extract()
        .response()
        .print();

    partner.setClientId("rewrite").setMethod("None").setUrlRewrites(List.of(new Partner.Rewrite()));

    given()
        .body(partner)
        .when()
        .put("/partners")
        .then()
        .statusCode(400)
        .extract()
        .response()
        .print();
  }

  @Test
  @Order(2)
  public void testPartnerImplementation() {
    var extraHeaders =
        Map.of(
            "x-doudou", new Partner.Header().setValue(Optional.of("god")),
            "x-uuid", new Partner.Header().setGenerator("Uuid"),
            "x-overwrite",
                new Partner.Header().setValue(Optional.of("overwrite")).setOverwrite(true));

    var partner =
        new Partner()
            .setBaseUrl(Optional.of(server.baseUrl() + "/"))
            .setClientId("none")
            .setMethod("None")
            .setExtraHeaders(extraHeaders);

    given().body(partner).when().put("/partners").then().statusCode(200);
  }

  @Test
  @Order(3)
  public void testParameters() {
    given()
        .header("x-overwrite", "doudou")
        .header("x-doudou", "doudou")
        .header("x-pass", "doudou")
        .header("x-request-id", "id of the request")
        .queryParam("d", "o")
        .queryParam("u", "d")
        .queryParam("o", "u")
        .when()
        .get("/proxy/none/api/none")
        .then()
        .statusCode(200)
        .body(is("OK for None method"));
  }

  @Test
  @Order(3)
  public void testRedirection() {
    given().when().get("/proxy/none/redirect").then().statusCode(200).body(is("OK for redirect"));
  }

  @Test
  @Order(3)
  public void testRewrite() {
    var partner =
        new Partner()
            .setBaseUrl(Optional.of("http://doudou.com"))
            .setClientId("rewrite")
            .setMethod("None")
            .setUrlRewrites(
                List.of(
                    new Partner.Rewrite()
                        .setMatches("http://doudou.com")
                        .setReplace(server.baseUrl()),
                    new Partner.Rewrite()
                        .setMatches("(https?:\\/\\/[^/]+)/cool")
                        .setReplace("$1/redirect")));

    given().body(partner).when().put("/partners").then().statusCode(200);

    given()
        .when()
        .get("/proxy/rewrite/redirect")
        .then()
        .statusCode(200)
        .body(is("OK for redirect"));
    given().when().get("/proxy/rewrite/cool").then().statusCode(200).body(is("OK for redirect"));
  }

  @Test
  @Order(3)
  public void testMultipartFormDataWithFile() throws IOException {

    // Simulate file and metadata
    String fileText = "This is a test file";
    String filename = "test.txt";
    byte[] fileBytes = fileText.getBytes();

    // Create a temporary file
    File file = File.createTempFile("test", ".txt");
    try (FileOutputStream fos = new FileOutputStream(file)) {
      fos.write(fileBytes);
    }

    given()
        .multiPart("data", "{\"filename\":\"" + filename + "}", "application/json")
        .multiPart("file", file)
        .when()
        .post("/proxy/none/upload")
        .then()
        .statusCode(200)
        .body("status", is("OK"));
  }
}

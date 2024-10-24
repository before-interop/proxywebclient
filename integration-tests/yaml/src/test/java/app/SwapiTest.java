package app;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class SwapiTest {

  @Test
  public void testLucSkywalker() {
    given()
        .when()
        .get("/proxy/swapi/people/1")
        .then()
        .statusCode(200)
        .body("name", is("Luke Skywalker"));
  }
}

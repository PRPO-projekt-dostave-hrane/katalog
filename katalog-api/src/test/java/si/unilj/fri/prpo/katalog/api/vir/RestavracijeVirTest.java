package si.unilj.fri.prpo.katalog.api.vir;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class RestavracijeVirTest {

    // GET /v1/restavracije

    @Test
    void seznamVrneVseRestavracije() {
        given()
                .when().get("/v1/restavracije")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("items", hasSize(3));
    }

    @Test
    void filterPoKuhinji() {
        given().queryParam("kuhinja", "japonska")
                .when().get("/v1/restavracije")
                .then().statusCode(200)
                .body("items", hasSize(1))
                .body("items[0].id", equalTo("r-2"));
    }

    @Test
    void paginacijaZOdmikom() {
        given().queryParam("limit", 1).queryParam("offset", 1)
                .when().get("/v1/restavracije")
                .then().statusCode(200)
                .body("items", hasSize(1))
                .body("items[0].id", equalTo("r-2"));
    }

    @Test
    void prevelikLimitVrne400() {
        given().queryParam("limit", 500)
                .when().get("/v1/restavracije")
                .then().statusCode(400)
                .contentType("application/problem+json")
                .body("status", equalTo(400))
                .body("errors[0].pointer", equalTo("limit"));
    }

    // GET /v1/restavracije/{restavracijaId}

    @Test
    void podrobnostiRestavracije() {
        given()
                .when().get("/v1/restavracije/r-1")
                .then().statusCode(200)
                .body("id", equalTo("r-1"))
                .body("ime", equalTo("Pizzeria Napoli"));
    }

    @Test
    void neobstojecaRestavracijaVrne404() {
        given()
                .when().get("/v1/restavracije/r-99")
                .then().statusCode(404)
                .contentType("application/problem+json")
                .body("type", endsWith("ni-najden"))
                .body("instance", equalTo("/v1/restavracije/r-99"));
    }
}

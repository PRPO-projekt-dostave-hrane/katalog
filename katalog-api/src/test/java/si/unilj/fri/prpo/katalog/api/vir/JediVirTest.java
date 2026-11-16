package si.unilj.fri.prpo.katalog.api.vir;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class JediVirTest {

    private static final String MENI = "/v1/restavracije/r-1/jedi";

    private static final String NOVA_JED = """
            {"ime": "Calzone", "cena": {"znesek": "11.00", "valuta": "EUR"}}
            """;

    /** Prebere trenutni ETag jedi, ki ga potrebujemo za If-Match. */
    private String etag(String jedId) {
        return given()
                .when().get(MENI + "/" + jedId)
                .then().statusCode(200)
                .extract().header("ETag");
    }

    // GET /v1/restavracije/{restavracijaId}/jedi

    @Test
    void meniRestavracije() {
        given()
                .when().get(MENI)
                .then().statusCode(200)
                .body("items.id", hasItems("j-1", "j-2"));
    }

    @Test
    void meniNeobstojeceRestavracijeVrne404() {
        given()
                .when().get("/v1/restavracije/r-99/jedi")
                .then().statusCode(404)
                .contentType("application/problem+json");
    }

    // POST /v1/restavracije/{restavracijaId}/jedi

    @Test
    void dodajanjeVrne201InLocation() {
        given().contentType(ContentType.JSON).body(NOVA_JED)
                .when().post(MENI)
                .then().statusCode(201)
                .header("Location", containsString(MENI + "/j-"))
                .body("id", notNullValue())
                .body("ime", equalTo("Calzone"))
                .body("razpolozljiva", equalTo(true));
    }

    @Test
    void dodajanjeVNeobstojecoRestavracijoVrne404() {
        given().contentType(ContentType.JSON).body(NOVA_JED)
                .when().post("/v1/restavracije/r-99/jedi")
                .then().statusCode(404);
    }

    @Test
    void neveljavenJsonVrne400() {
        given().contentType(ContentType.JSON).body("{ to ni json")
                .when().post(MENI)
                .then().statusCode(400)
                .contentType("application/problem+json");
    }

    @Test
    void manjkajoceImeVrne422() {
        given().contentType(ContentType.JSON)
                .body("""
                        {"cena": {"znesek": "11.00", "valuta": "EUR"}}
                        """)
                .when().post(MENI)
                .then().statusCode(422)
                .contentType("application/problem+json")
                .body("type", endsWith("vnos"))
                .body("errors.pointer", hasItems("#/ime"));
    }

    // GET /v1/restavracije/{restavracijaId}/jedi/{jedId}

    @Test
    void podrobnostiJediVrnejoETag() {
        given()
                .when().get(MENI + "/j-1")
                .then().statusCode(200)
                .header("ETag", notNullValue())
                .body("ime", equalTo("Margherita"));
    }

    @Test
    void neobstojecaJedVrne404() {
        given()
                .when().get(MENI + "/j-99")
                .then().statusCode(404)
                .body("type", endsWith("ni-najden"));
    }

    // PATCH /v1/restavracije/{restavracijaId}/jedi/{jedId}

    @Test
    void spremembaZaPravimETagomVrne200() {
        String staroEtag = etag("j-2");

        given().contentType(ContentType.JSON).header("If-Match", staroEtag)
                .body("""
                        {"razpolozljiva": false}
                        """)
                .when().patch(MENI + "/j-2")
                .then().statusCode(200)
                .body("razpolozljiva", equalTo(false))
                .header("ETag", notNullValue());
    }

    @Test
    void spremembaBrezIfMatchVrne428() {
        given().contentType(ContentType.JSON)
                .body("""
                        {"razpolozljiva": false}
                        """)
                .when().patch(MENI + "/j-1")
                .then().statusCode(428)
                .body("type", endsWith("obvezen-pogoj"));
    }

    @Test
    void spremembaZNapacnimETagomVrne412() {
        given().contentType(ContentType.JSON).header("If-Match", "\"napacen\"")
                .body("""
                        {"razpolozljiva": false}
                        """)
                .when().patch(MENI + "/j-1")
                .then().statusCode(412)
                .body("type", endsWith("verzija"));
    }

    @Test
    void spremembaNeobstojeceJediVrne404() {
        given().contentType(ContentType.JSON).header("If-Match", "\"karkoli\"")
                .body("""
                        {"razpolozljiva": false}
                        """)
                .when().patch(MENI + "/j-99")
                .then().statusCode(404);
    }

    @Test
    void spremembaZNeveljavnimJsonomVrne400() {
        given().contentType(ContentType.JSON).header("If-Match", "\"karkoli\"")
                .body("{ to ni json")
                .when().patch(MENI + "/j-1")
                .then().statusCode(400);
    }

    @Test
    void spremembaZNeveljavnoCenoVrne422() {
        given().contentType(ContentType.JSON).header("If-Match", etag("j-1"))
                .body("""
                        {"cena": {"znesek": "devet", "valuta": "EUR"}}
                        """)
                .when().patch(MENI + "/j-1")
                .then().statusCode(422)
                .body("errors.pointer", hasItems("#/cena/znesek"));
    }

    // DELETE /v1/restavracije/{restavracijaId}/jedi/{jedId}

    @Test
    void brisanjeVrne204InNatoNeObstaja() {
        String id = given().contentType(ContentType.JSON).body(NOVA_JED)
                .when().post(MENI)
                .then().statusCode(201)
                .extract().path("id");

        given().when().delete(MENI + "/" + id)
                .then().statusCode(204);

        given().when().delete(MENI + "/" + id)
                .then().statusCode(404);
    }
}

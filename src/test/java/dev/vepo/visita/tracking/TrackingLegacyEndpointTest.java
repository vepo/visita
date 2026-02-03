package dev.vepo.visita.tracking;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.vepo.infra.Given;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response.Status;

@QuarkusTest
class TrackingLegacyEndpointTest {

    @BeforeEach
    void setup() {
        Given.cleanDatabase();
        Given.domain()
             .withHostname("blog.vepo.dev")
             .withToken("token")
             .persist();
    }

    @Test
    void access_successTest() {
        var request = new TrackingStartRequest("en",
                                               "http://blog.vepo.dev/post/post-1",
                                               "www.google.com",
                                               "1920x1080",
                                               "tab-1",
                                               System.currentTimeMillis(),
                                               "America/Sao_Paulo",
                                               "Mozilla/5.0 Test",
                                               "user-1");

        given().contentType(ContentType.JSON)
               .body(request)
               .when()
               .post("/api/visita/access")
               .then()
               .statusCode(Status.OK.getStatusCode())
               .body("id", greaterThan(0))
               .extract()
               .path("id");
    }

    @Test
    void exit_successTest() {
        // First create a tracking session
        var createRequest = new TrackingStartRequest("en",
                                                     "http://blog.vepo.dev/post/post-1",
                                                     "www.google.com",
                                                     "1920x1080",
                                                     "tab-1",
                                                     System.currentTimeMillis(),
                                                     "America/Sao_Paulo",
                                                     "Mozilla/5.0 Test",
                                                     "user-1");

        int viewId = given().contentType(ContentType.JSON)
                            .body(createRequest)
                            .when()
                            .post("/api/visita/access")
                            .then()
                            .extract()
                            .path("id");

        // Then exit the session
        var exitRequest = new TrackingEndRequest(viewId, System.currentTimeMillis());

        given().contentType(ContentType.JSON)
               .body(exitRequest)
               .when()
               .post("/api/visita/exit")
               .then()
               .statusCode(Status.OK.getStatusCode());
    }

    @Test
    void exit_invalidIdTest() {
        var exitRequest = new TrackingEndRequest(999999L, System.currentTimeMillis());

        given().contentType(ContentType.JSON)
               .body(exitRequest)
               .when()
               .post("/api/visita/exit")
               .then()
               .statusCode(Status.OK.getStatusCode()); // Note: The endpoint doesn't validate if ID exists
    }

    @Test
    void view_successTest() {
        // First create a tracking session
        var createRequest = new TrackingStartRequest("en",
                                                     "http://blog.vepo.dev/post/post-1",
                                                     "www.google.com",
                                                     "1920x1080",
                                                     "tab-1",
                                                     System.currentTimeMillis(),
                                                     "America/Sao_Paulo",
                                                     "Mozilla/5.0 Test",
                                                     "user-1");

        int viewId = given().contentType(ContentType.JSON)
                            .body(createRequest)
                            .when()
                            .post("/api/visita/access")
                            .then()
                            .extract()
                            .path("id");

        // Then update the view
        var updateRequest = new TrackingUpdateRequest(viewId,
                                                      "http://blog.vepo.dev/post/post-2",
                                                      "tab-1",
                                                      System.currentTimeMillis() + 1000,
                                                      "user-1");

        given().contentType(ContentType.JSON)
               .body(updateRequest)
               .when()
               .post("/api/visita/view")
               .then()
               .statusCode(Status.OK.getStatusCode())
               .body("id", not(is((int) viewId)));
    }

    @Test
    void ping_successTest() {
        // First create a tracking session
        var createRequest = new TrackingStartRequest("en",
                                                     "http://blog.vepo.dev/post/post-1",
                                                     "www.google.com",
                                                     "1920x1080",
                                                     "tab-1",
                                                     System.currentTimeMillis(),
                                                     "America/Sao_Paulo",
                                                     "Mozilla/5.0 Test",
                                                     "user-1");

        int viewId = given().contentType(ContentType.JSON)
                            .body(createRequest)
                            .when()
                            .post("/api/visita/access")
                            .then()
                            .extract()
                            .path("id");

        // Then send ping
        var pingRequest = new TrackingPingRequest(viewId, System.currentTimeMillis() + 5000);

        given().contentType(ContentType.JSON)
               .body(pingRequest)
               .when()
               .post("/api/visita/ping")
               .then()
               .statusCode(Status.OK.getStatusCode());
    }
}
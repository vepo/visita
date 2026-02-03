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
class TrackingEndpointTest {

    private static final String VALID_TOKEN = "TOKEN_1234";
    private static final String VALID_HOSTNAME = "blog.vepo.dev";
    private static final String INVALID_TOKEN = "TOKEN_5678";
    private static final String INVALID_HOSTNAME = "invalid.vepo.dev";

    @BeforeEach
    void setup() {
        Given.cleanDatabase();
        Given.domain()
             .withHostname(VALID_HOSTNAME)
             .withToken(VALID_TOKEN)
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

        given().header(TrackingTokenFilter.TOKEN_HEADER, VALID_TOKEN)
               .header(TrackingTokenFilter.HOSTNAME_HEADER, VALID_HOSTNAME)
               .contentType(ContentType.JSON)
               .body(request)
               .when()
               .post("/api/tracking/access")
               .then()
               .statusCode(Status.OK.getStatusCode())
               .body("id", greaterThan(0))
               .extract()
               .path("id");
    }

    @Test
    void access_noTokenTest() {
        var request = new TrackingStartRequest("en",
                                               "http://blog.vepo.dev/post/post-1",
                                               "www.google.com",
                                               "1920x1080",
                                               "tab-1",
                                               System.currentTimeMillis(),
                                               "America/Sao_Paulo",
                                               "Mozilla/5.0 Test",
                                               "user-1");

        given().header(TrackingTokenFilter.HOSTNAME_HEADER, VALID_HOSTNAME)
               .contentType(ContentType.JSON)
               .body(request)
               .when()
               .post("/api/tracking/access")
               .then()
               .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void access_noHostnameTest() {
        var request = new TrackingStartRequest("en",
                                               "http://blog.vepo.dev/post/post-1",
                                               "www.google.com",
                                               "1920x1080",
                                               "tab-1",
                                               System.currentTimeMillis(),
                                               "America/Sao_Paulo",
                                               "Mozilla/5.0 Test",
                                               "user-1");

        given().header(TrackingTokenFilter.TOKEN_HEADER, VALID_TOKEN)
               .contentType(ContentType.JSON)
               .body(request)
               .when()
               .post("/api/tracking/access")
               .then()
               .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void access_invalidTokenTest() {
        var request = new TrackingStartRequest("en",
                                               "http://blog.vepo.dev/post/post-1",
                                               "www.google.com",
                                               "1920x1080",
                                               "tab-1",
                                               System.currentTimeMillis(),
                                               "America/Sao_Paulo",
                                               "Mozilla/5.0 Test",
                                               "user-1");

        given().header(TrackingTokenFilter.TOKEN_HEADER, INVALID_TOKEN)
               .header(TrackingTokenFilter.HOSTNAME_HEADER, VALID_HOSTNAME)
               .contentType(ContentType.JSON)
               .body(request)
               .when()
               .post("/api/tracking/access")
               .then()
               .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void access_invalidHostnameTest() {
        var request = new TrackingStartRequest("en",
                                               "http://blog.vepo.dev/post/post-1",
                                               "www.google.com",
                                               "1920x1080",
                                               "tab-1",
                                               System.currentTimeMillis(),
                                               "America/Sao_Paulo",
                                               "Mozilla/5.0 Test",
                                               "user-1");

        given().header(TrackingTokenFilter.TOKEN_HEADER, VALID_TOKEN)
               .header(TrackingTokenFilter.HOSTNAME_HEADER, INVALID_HOSTNAME)
               .contentType(ContentType.JSON)
               .body(request)
               .when()
               .post("/api/tracking/access")
               .then()
               .statusCode(Status.UNAUTHORIZED.getStatusCode());
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

        int viewId = given().header(TrackingTokenFilter.TOKEN_HEADER, VALID_TOKEN)
                            .header(TrackingTokenFilter.HOSTNAME_HEADER, VALID_HOSTNAME)
                            .contentType(ContentType.JSON)
                            .body(createRequest)
                            .when()
                            .post("/api/tracking/access")
                            .then()
                            .extract()
                            .path("id");

        // Then exit the session
        var exitRequest = new TrackingEndRequest(viewId, System.currentTimeMillis());

        given().header(TrackingTokenFilter.TOKEN_HEADER, VALID_TOKEN)
               .header(TrackingTokenFilter.HOSTNAME_HEADER, VALID_HOSTNAME)
               .contentType(ContentType.JSON)
               .body(exitRequest)
               .when()
               .post("/api/tracking/exit")
               .then()
               .statusCode(Status.OK.getStatusCode());
    }

    @Test
    void exit_invalidIdTest() {
        var exitRequest = new TrackingEndRequest(999999L, System.currentTimeMillis());

        given().header(TrackingTokenFilter.TOKEN_HEADER, VALID_TOKEN)
               .header(TrackingTokenFilter.HOSTNAME_HEADER, VALID_HOSTNAME)
               .contentType(ContentType.JSON)
               .body(exitRequest)
               .when()
               .post("/api/tracking/exit")
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

        int viewId = given().header(TrackingTokenFilter.TOKEN_HEADER, VALID_TOKEN)
                            .header(TrackingTokenFilter.HOSTNAME_HEADER, VALID_HOSTNAME)
                            .contentType(ContentType.JSON)
                            .body(createRequest)
                            .when()
                            .post("/api/tracking/access")
                            .then()
                            .extract()
                            .path("id");

        // Then update the view
        var updateRequest = new TrackingUpdateRequest(viewId,
                                                      "http://blog.vepo.dev/post/post-2",
                                                      "tab-1",
                                                      System.currentTimeMillis() + 1000,
                                                      "user-1");

        given().header(TrackingTokenFilter.TOKEN_HEADER, VALID_TOKEN)
               .header(TrackingTokenFilter.HOSTNAME_HEADER, VALID_HOSTNAME)
               .contentType(ContentType.JSON)
               .body(updateRequest)
               .when()
               .post("/api/tracking/view")
               .then()
               .statusCode(Status.OK.getStatusCode())
               .body("id", not(is((int) viewId)));
    }

    @Test
    void view_notFoundTest() {
        var updateRequest = new TrackingUpdateRequest(999999L,
                                                      "http://blog.vepo.dev/post/post-2",
                                                      "tab-1",
                                                      System.currentTimeMillis(),
                                                      "user-1");

        given().header(TrackingTokenFilter.TOKEN_HEADER, VALID_TOKEN)
               .header(TrackingTokenFilter.HOSTNAME_HEADER, VALID_HOSTNAME)
               .contentType(ContentType.JSON)
               .body(updateRequest)
               .when()
               .post("/api/tracking/view")
               .then()
               .statusCode(Status.NOT_FOUND.getStatusCode());
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

        int viewId = given().header(TrackingTokenFilter.TOKEN_HEADER, VALID_TOKEN)
                            .header(TrackingTokenFilter.HOSTNAME_HEADER, VALID_HOSTNAME)
                            .contentType(ContentType.JSON)
                            .body(createRequest)
                            .when()
                            .post("/api/tracking/access")
                            .then()
                            .extract()
                            .path("id");

        // Then send ping
        var pingRequest = new TrackingPingRequest(viewId, System.currentTimeMillis() + 5000);

        given().header(TrackingTokenFilter.TOKEN_HEADER, VALID_TOKEN)
               .header(TrackingTokenFilter.HOSTNAME_HEADER, VALID_HOSTNAME)
               .contentType(ContentType.JSON)
               .body(pingRequest)
               .when()
               .post("/api/tracking/ping")
               .then()
               .statusCode(Status.OK.getStatusCode());
    }

    @Test
    void ping_invalidIdTest() {
        var pingRequest = new TrackingPingRequest(999999L, System.currentTimeMillis());

        given().header(TrackingTokenFilter.TOKEN_HEADER, VALID_TOKEN)
               .header(TrackingTokenFilter.HOSTNAME_HEADER, VALID_HOSTNAME)
               .contentType(ContentType.JSON)
               .body(pingRequest)
               .when()
               .post("/api/tracking/ping")
               .then()
               .statusCode(Status.OK.getStatusCode()); // Note: The endpoint doesn't validate if ID exists
    }

    @Test
    void validation_invalidTimestampTest() {
        var request = new TrackingStartRequest("en",
                                               "http://blog.vepo.dev/post/post-1",
                                               "www.google.com",
                                               "1920x1080",
                                               "tab-1",
                                               0L, // Invalid timestamp (less than 1)
                                               "America/Sao_Paulo",
                                               "Mozilla/5.0 Test",
                                               "user-1");

        given().header(TrackingTokenFilter.TOKEN_HEADER, VALID_TOKEN)
               .header(TrackingTokenFilter.HOSTNAME_HEADER, VALID_HOSTNAME)
               .contentType(ContentType.JSON)
               .body(request)
               .when()
               .post("/api/tracking/access")
               .then()
               .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void validation_blankPageTest() {
        var request = new TrackingStartRequest("en",
                                               "", // Blank page
                                               "www.google.com",
                                               "1920x1080",
                                               "tab-1",
                                               System.currentTimeMillis(),
                                               "America/Sao_Paulo",
                                               "Mozilla/5.0 Test",
                                               "user-1");

        given().header(TrackingTokenFilter.TOKEN_HEADER, VALID_TOKEN)
               .header(TrackingTokenFilter.HOSTNAME_HEADER, VALID_HOSTNAME)
               .contentType(ContentType.JSON)
               .body(request)
               .when()
               .post("/api/tracking/access")
               .then()
               .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void validation_blankUserIdTest() {
        var request = new TrackingStartRequest("en",
                                               "http://blog.vepo.dev/post/post-1",
                                               "www.google.com",
                                               "1920x1080",
                                               "tab-1",
                                               System.currentTimeMillis(),
                                               "America/Sao_Paulo",
                                               "Mozilla/5.0 Test",
                                               "" // Blank user ID
        );

        given().header(TrackingTokenFilter.TOKEN_HEADER, VALID_TOKEN)
               .header(TrackingTokenFilter.HOSTNAME_HEADER, VALID_HOSTNAME)
               .contentType(ContentType.JSON)
               .body(request)
               .when()
               .post("/api/tracking/access")
               .then()
               .statusCode(Status.BAD_REQUEST.getStatusCode());
    }
}
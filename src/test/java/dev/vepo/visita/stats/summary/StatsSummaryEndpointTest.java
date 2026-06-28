package dev.vepo.visita.stats.summary;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.vepo.infra.Given;
import dev.vepo.infra.Given.User;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response.Status;

@QuarkusTest
class StatsSummaryEndpointTest {
    private User admin;
    private User statsViewer;
    private User nonAdmin;

    @BeforeEach
    void setup() {
        Given.cleanDatabase();
        this.admin = Given.admin();
        this.statsViewer = Given.statsViewer();
        this.nonAdmin = Given.nonAdmin();

        Given.domain()
             .withHostname("blog.vepo.dev")
             .withToken("token-1")
             .persist();
        Given.view()
             .withPage("https://blog.vepo.dev/")
             .withLength(30)
             .persist();
    }

    @Test
    void shouldReturnSummaryForDomainAdmin() {
        given().header(admin.authenticated())
               .accept(ContentType.JSON)
               .when()
               .get("/api/stats/summary")
               .then()
               .statusCode(Status.OK.getStatusCode())
               .body("totalViews", greaterThan(0))
               .body("monitoredPages", greaterThan(0))
               .body("topDomains", hasSize(greaterThan(0)));
    }

    @Test
    void shouldReturnSummaryForStatsViewer() {
        given().header(statsViewer.authenticated())
               .accept(ContentType.JSON)
               .when()
               .get("/api/stats/summary")
               .then()
               .statusCode(Status.OK.getStatusCode())
               .body("totalViews", greaterThan(0));
    }

    @Test
    void shouldRejectSummaryWithoutRole() {
        given().header(nonAdmin.authenticated())
               .accept(ContentType.JSON)
               .when()
               .get("/api/stats/summary")
               .then()
               .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void shouldAcceptDateRange() {
        var startDate = LocalDate.now().minusDays(30);
        var endDate = LocalDate.now();

        given().header(admin.authenticated())
               .accept(ContentType.JSON)
               .queryParam("startDate", startDate.toString())
               .queryParam("endDate", endDate.toString())
               .when()
               .get("/api/stats/summary")
               .then()
               .statusCode(Status.OK.getStatusCode())
               .body("daysInRange", greaterThan(0));
    }
}

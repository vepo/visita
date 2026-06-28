package dev.vepo.visita.domain.update;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.vepo.infra.Given;
import dev.vepo.infra.Given.User;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response.Status;

@QuarkusTest
class UpdateDomainEndpointTest {
    private static final String VALID_HOSTNAME = "blog.vepo.dev";
    private User admin;

    private long domainId;

    @BeforeEach
    void setup() {
        Given.cleanDatabase();
        this.admin = Given.admin();
        Given.domain()
             .withHostname(VALID_HOSTNAME)
             .withToken("TOKEN_1234")
             .persist();
        this.domainId = Given.withTransaction(() -> Given.inject(dev.vepo.visita.domain.DomainRepository.class)
                                                         .findByHostname(VALID_HOSTNAME)
                                                         .orElseThrow()
                                                         .getId());
    }

    @Test
    void shouldRejectInvalidIgnoredPathPattern() {
        given().header(admin.authenticated())
               .contentType(ContentType.JSON)
               .accept(ContentType.JSON)
               .body(new UpdateDomainRequest(VALID_HOSTNAME, java.util.List.of("[invalid")))
               .when()
               .put("/api/domains/%d".formatted(domainId))
               .then()
               .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void shouldPersistIgnoredPathPatterns() {
        given().header(admin.authenticated())
               .contentType(ContentType.JSON)
               .accept(ContentType.JSON)
               .body(new UpdateDomainRequest(VALID_HOSTNAME, java.util.List.of("/admin/.*")))
               .when()
               .put("/api/domains/%d".formatted(domainId))
               .then()
               .statusCode(Status.OK.getStatusCode())
               .body("ignoredPathPatterns[0]", is("/admin/.*"));
    }
}

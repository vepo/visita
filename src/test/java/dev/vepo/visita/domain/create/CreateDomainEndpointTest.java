package dev.vepo.visita.domain.create;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.vepo.infra.Given;
import dev.vepo.infra.Given.User;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response.Status;

@QuarkusTest
class CreateDomainEndpointTest {
    private static final String VALID_TOKEN = "TOKEN_1234";
    private static final String VALID_HOSTNAME = "blog.vepo.dev";
    private User admin;
    private User nonAdmin;

    @BeforeEach
    void setup() {
        Given.cleanDatabase();
        this.admin = Given.admin();
        this.nonAdmin = Given.nonAdmin();
    }

    @Test
    void createDomainTest() {
        given().header(admin.authenticated())
               .contentType(ContentType.JSON)
               .accept(ContentType.JSON)
               .body(new CreateDomainRequest(VALID_HOSTNAME))
               .when()
               .post("/api/domains")
               .then()
               .statusCode(Status.OK.getStatusCode())
               .body("id", greaterThan(0))
               .body("hostname", not(empty()))
               .body("token", not(empty()));
    }

    @Test
    void createDomainRequiresAdminTest() {
        given().header(nonAdmin.authenticated())
               .contentType(ContentType.JSON)
               .accept(ContentType.JSON)
               .body(new CreateDomainRequest(VALID_HOSTNAME))
               .when()
               .post("/api/domains")
               .then()
               .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void createDomainValidateIfDomainExistTest() {
        Given.domain()
             .withHostname(VALID_HOSTNAME)
             .withToken(VALID_TOKEN)
             .persist();
        given().header(admin.authenticated())
               .contentType(ContentType.JSON)
               .accept(ContentType.JSON)
               .body(new CreateDomainRequest(VALID_HOSTNAME))
               .when()
               .post("/api/domains")
               .then()
               .statusCode(Status.CONFLICT.getStatusCode());
    }
}

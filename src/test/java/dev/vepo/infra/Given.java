package dev.vepo.infra;

import static org.junit.jupiter.api.Assertions.fail;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.microprofile.config.ConfigProvider;

import dev.vepo.visita.shared.security.RequiredRoles;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.restassured.http.Header;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.persistence.EntityManager;

public abstract class Given {
    private Given() {
        throw new IllegalStateException("Utility class!");
    }

    public static <T> T inject(Class<T> clazz) {
        return CDI.current().select(clazz).get();
    }

    public static void cleanDatabase() {
        withTransaction(() -> inject(GivenRepository.class).cleanup());
    }

    public static void withTransaction(Runnable block) {
        try {
            QuarkusTransaction.begin();
            block.run();
            QuarkusTransaction.commit();
        } catch (Exception e) {
            QuarkusTransaction.rollback();
            fail("Fail to create transaction!", e);
        } finally {
            inject(EntityManager.class).clear();
        }
    }

    public static <T> T withTransaction(Supplier<T> block) {
        try {
            QuarkusTransaction.begin();
            var obj = block.get();
            QuarkusTransaction.commit();

            return obj;
        } catch (Exception e) {
            QuarkusTransaction.rollback();
            fail("Fail to create transaction!", e);
            return null;
        } finally {
            inject(EntityManager.class).clear();
        }
    }

    public static ViewBuilder view() {
        return new ViewBuilder();
    }

    public static DomainBuilder domain() {
        return new DomainBuilder();
    }

    public record User(long id, String username, String email, Set<String> roles) {
        public Header authenticated() {
            Instant now = Instant.now();
            var token = Jwt.issuer(ConfigProvider.getConfig().getValue("mp.jwt.verify.issuer", String.class))
                           .upn(username)
                           .claim("username", username)
                           .claim("id", id)
                           .claim("email", email)
                           .groups(roles)
                           .issuedAt(now)
                           .expiresAt(now.plus(1, ChronoUnit.DAYS))
                           .sign();
            return new Header("Authorization", "Bearer %s".formatted(token));
        }
    }

    public static User admin() {
        return new User(1, "admin", "admin@passport.vepo.dev", Set.of(RequiredRoles.ADMIN));
    }

    public static User nonAdmin() {
        return new User(2, "user", "user@passport.vepo.dev", Set.of());
    }
}

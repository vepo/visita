package dev.vepo.infra;

import static org.junit.jupiter.api.Assertions.fail;

import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.inject.spi.CDI;

public abstract class Given {
    private Given() {
        throw new IllegalStateException("Utility class!");
    }

    public static <T> T inject(Class<T> clazz) {
        return CDI.current().select(clazz).get();
    }

    public static void cleanDatabase() {
        try {
            QuarkusTransaction.begin();
            inject(GivenRepository.class).cleanup();
            QuarkusTransaction.commit();
        } catch (Exception e) {
            QuarkusTransaction.rollback();
            fail("Fail to create transaction!", e);
        }
    }
}

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
        }
    }

    public static ViewBuilder visita() {
        return new ViewBuilder();
    }
}

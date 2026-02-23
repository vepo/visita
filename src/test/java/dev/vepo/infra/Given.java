package dev.vepo.infra;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.function.Supplier;

import io.quarkus.narayana.jta.QuarkusTransaction;
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
}

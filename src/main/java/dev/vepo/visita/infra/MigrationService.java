package dev.vepo.visita.infra;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MigrationService {
    private static final Logger logger = LoggerFactory.getLogger(MigrationService.class);
    private final Flyway flyway;

    @Inject
    public MigrationService(Flyway flyway) {
        this.flyway = flyway;
    }

    public void checkMigration() {
        logger.info("Using database at {}", flyway.info().current().getVersion());
    }
}
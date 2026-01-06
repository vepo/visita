package dev.vepo.infra;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class GivenRepository {
    private final EntityManager entityManager;

    @Inject
    public GivenRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void cleanup() {
        this.entityManager.createQuery("DELETE FROM Visita").executeUpdate();
    }
}

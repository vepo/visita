package dev.vepo.visita.domain;

import java.util.Objects;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class DomainRepository {
    private final EntityManager entityManager;

    @Inject
    public DomainRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<Domain> findByHostname(String hostname) {
        return entityManager.createQuery("FROM Domain WHERE hostname = :hostname", Domain.class)
                            .setParameter("hostname", hostname)
                            .getResultStream()
                            .findFirst();
    }

    public Domain save(Domain domain) {
        Objects.requireNonNull(domain, "'domain' cannot be null!");
        this.entityManager.persist(domain);
        return domain;
    }
}

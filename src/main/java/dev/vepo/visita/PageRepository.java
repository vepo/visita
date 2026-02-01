package dev.vepo.visita;

import java.util.Objects;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class PageRepository {
    private final EntityManager entityManager;

    @Inject
    public PageRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<Page> findByHostnameAndPath(String hostname, String path) {
        return entityManager.createQuery("FROM Page WHERE domain.hostname = :hostname AND path = :path", Page.class)
                            .setParameter("hostname", hostname)
                            .setParameter("path", path)
                            .getResultStream()
                            .findFirst();
    }

    public Page save(Page page) {
        Objects.requireNonNull(page, "'page' cannot be null!");
        this.entityManager.persist(page);
        return page;
    }

}

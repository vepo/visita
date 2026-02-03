package dev.vepo.visita;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class ViewRepository {
    private final EntityManager entityManager;

    @Inject
    public ViewRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public View save(View visita) {
        this.entityManager.persist(visita);
        return visita;
    }

    public View findById(Long id) {
        return this.entityManager.find(View.class, id);
    }

    public List<View> findAll() {
        return entityManager.createQuery("FROM View", View.class)
                            .getResultList();
    }
}

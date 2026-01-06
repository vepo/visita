package dev.vepo.visita;

import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class VisitaRepository {
    private final EntityManager entityManager;

    @Inject
    public VisitaRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Visita save(Visita visita) {
        this.entityManager.persist(visita);
        return visita;
    }

    public Visita findById(Long id) {
        return this.entityManager.find(Visita.class, id);
    }

    public List<VisitaDiaria> findDailyViews() {
        return entityManager.createQuery("""
                                         SELECT DATE(v.dataAcesso) as data,
                                                COUNT(v.id) as visitas,
                                                AVG(v.duracao) as tempoMedio
                                         FROM Visita v
                                         WHERE v.dataAcesso IS NOT NULL AND v.duracao IS NOT NULL
                                         GROUP BY DATE(v.dataAcesso)
                                         ORDER BY DATE(v.dataAcesso) DESC
                                         """, Object[].class)
                            .getResultStream()
                            .map(result -> new VisitaDiaria(result[0].toString(),
                                                            (Long) result[1],
                                                            result[2] != null ? Math.round((Double) result[2]) : 0L))
                            .toList();
    }

    public List<Map<String, Object>> findPageViews() {
        return entityManager.createQuery("""
                                         SELECT v.pagina,
                                                COUNT(v.id) as visitas,
                                                AVG(v.duracao) as tempoMedio
                                         FROM Visita v
                                         WHERE v.pagina IS NOT NULL AND v.duracao IS NOT NULL
                                         GROUP BY v.pagina
                                         ORDER BY visitas DESC
                                         """, Object[].class)
                            .getResultStream()
                            .map(result -> Map.of("pagina", result[0],
                                                  "visitas", result[1],
                                                  "tempoMedio", result[2] != null ? Math.round((Double) result[2]) : 0L))
                            .toList();
    }

    public List<Visita> findAll() {
        return entityManager.createQuery("FROM Visita", Visita.class)
                            .getResultList();
    }
}

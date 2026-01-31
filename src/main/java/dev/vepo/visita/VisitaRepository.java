package dev.vepo.visita;

import java.time.LocalDateTime;
import java.util.List;

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

    public List<EstatisticaPorDia> findDailyViews() {
        return entityManager.createQuery("""
                                         SELECT new EstatisticaPorDia(DATE(v.dataAcesso),
                                                                      COUNT(v.id),
                                                                      AVG(v.duracao),
                                                                      PERCENTILE_CONT(0.7) WITHIN GROUP (ORDER BY v.duracao),
                                                                      PERCENTILE_CONT(0.9) WITHIN GROUP (ORDER BY v.duracao))
                                         FROM Visita v
                                         WHERE v.dataAcesso IS NOT NULL AND v.duracao IS NOT NULL
                                         GROUP BY DATE(v.dataAcesso)
                                         ORDER BY DATE(v.dataAcesso) DESC
                                         """, EstatisticaPorDia.class)
                            .getResultStream()
                            .toList();
    }

    public List<EstatisticaPorPagina> findPageViews(LocalDateTime startDate) {
        if (startDate == LocalDateTime.MIN) {
            return entityManager.createQuery("""
                                             SELECT new EstatisticaPorPagina(v.pagina,
                                                                             COUNT(v.id) as visitas,
                                                                             AVG(v.duracao) as tempoMedio,
                                                                             PERCENTILE_CONT(0.7) WITHIN GROUP (ORDER BY v.duracao) as tempoMedioPerc50,
                                                                             PERCENTILE_CONT(0.9) WITHIN GROUP (ORDER BY v.duracao) as tempoMedioPerc90)
                                             FROM Visita v
                                             WHERE v.pagina IS NOT NULL AND v.duracao IS NOT NULL
                                             GROUP BY v.pagina
                                             ORDER BY visitas DESC
                                             """, EstatisticaPorPagina.class)
                                .getResultStream()
                                .toList();
        } else {
            return entityManager.createQuery("""
                                             SELECT new EstatisticaPorPagina(v.pagina,
                                                                             COUNT(v.id) as visitas,
                                                                             AVG(v.duracao) as tempoMedio,
                                                                             PERCENTILE_CONT(0.7) WITHIN GROUP (ORDER BY v.duracao) as tempoMedioPerc50,
                                                                             PERCENTILE_CONT(0.9) WITHIN GROUP (ORDER BY v.duracao) as tempoMedioPerc90)
                                             FROM Visita v
                                             WHERE v.pagina IS NOT NULL AND v.duracao IS NOT NULL AND v.dataAcesso >= :start_date
                                             GROUP BY v.pagina
                                             ORDER BY visitas DESC
                                             """, EstatisticaPorPagina.class)
                                .setParameter("start_date", startDate)
                                .getResultStream()
                                .toList();
        }
    }

    public List<Visita> findAll() {
        return entityManager.createQuery("FROM Visita", Visita.class)
                            .getResultList();
    }
}

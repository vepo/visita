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

    public View save(View visita) {
        this.entityManager.persist(visita);
        return visita;
    }

    public View findById(Long id) {
        return this.entityManager.find(View.class, id);
    }

    public List<DailyStats> findDailyViews() {
        return entityManager.createQuery("""
                                         SELECT new DailyStats(DATE(v.dataAcesso),
                                                               COUNT(v.id),
                                                               AVG(v.duracao),
                                                               PERCENTILE_CONT(0.7) WITHIN GROUP (ORDER BY v.duracao),
                                                               PERCENTILE_CONT(0.9) WITHIN GROUP (ORDER BY v.duracao))
                                         FROM View v
                                         WHERE v.dataAcesso IS NOT NULL AND v.duracao IS NOT NULL
                                         GROUP BY DATE(v.dataAcesso)
                                         ORDER BY DATE(v.dataAcesso) DESC
                                         """, DailyStats.class)
                            .getResultStream()
                            .toList();
    }

    public List<PageStats> findPageViews(LocalDateTime startDate) {
        if (startDate == LocalDateTime.MIN) {
            return entityManager.createQuery("""
                                             SELECT new PageStats(v.pagina,
                                                                  COUNT(v.id) as views,
                                                                  AVG(v.duracao) as avgDuration,
                                                                  PERCENTILE_CONT(0.7) WITHIN GROUP (ORDER BY v.duracao) as avgDurationPerc50,
                                                                  PERCENTILE_CONT(0.9) WITHIN GROUP (ORDER BY v.duracao) as avgDurationPerc90)
                                             FROM View v
                                             WHERE v.pagina IS NOT NULL AND v.duracao IS NOT NULL
                                             GROUP BY v.pagina
                                             ORDER BY views DESC
                                             """, PageStats.class)
                                .getResultStream()
                                .toList();
        } else {
            return entityManager.createQuery("""
                                             SELECT new PageStats(v.pagina,
                                                                  COUNT(v.id) as views,
                                                                  AVG(v.duracao) as avgDuration,
                                                                  PERCENTILE_CONT(0.7) WITHIN GROUP (ORDER BY v.duracao) as avgDurationPerc50,
                                                                  PERCENTILE_CONT(0.9) WITHIN GROUP (ORDER BY v.duracao) as avgDurationPerc90)
                                             FROM View v
                                             WHERE v.pagina IS NOT NULL AND v.duracao IS NOT NULL AND v.dataAcesso >= :start_date
                                             GROUP BY v.pagina
                                             ORDER BY views DESC
                                             """, PageStats.class)
                                .setParameter("start_date", startDate)
                                .getResultStream()
                                .toList();
        }
    }

    public List<View> findAll() {
        return entityManager.createQuery("FROM View", View.class)
                            .getResultList();
    }
}

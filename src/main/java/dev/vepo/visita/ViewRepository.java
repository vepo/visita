package dev.vepo.visita;

import java.time.LocalDateTime;
import java.util.List;

import dev.vepo.visita.dashboard.DailyStats;
import dev.vepo.visita.dashboard.PageStats;
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

    public List<DailyStats> findDailyViews() {
        return entityManager.createQuery("""
                                         SELECT new DailyStats(DATE(v.accessTimestamp),
                                                               COUNT(v.id),
                                                               AVG(v.length),
                                                               PERCENTILE_CONT(0.7) WITHIN GROUP (ORDER BY v.length),
                                                               PERCENTILE_CONT(0.9) WITHIN GROUP (ORDER BY v.length))
                                         FROM View v
                                         WHERE v.accessTimestamp IS NOT NULL AND v.length IS NOT NULL
                                         GROUP BY DATE(v.accessTimestamp)
                                         ORDER BY DATE(v.accessTimestamp) DESC
                                         """, DailyStats.class)
                            .getResultStream()
                            .toList();
    }

    public List<PageStats> findPageViews(LocalDateTime startDate) {
        if (startDate == LocalDateTime.MIN) {
            return entityManager.createQuery("""
                                             SELECT new PageStats(v.page,
                                                                  COUNT(v.id) as views,
                                                                  AVG(v.length) as avgDuration,
                                                                  PERCENTILE_CONT(0.7) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc50,
                                                                  PERCENTILE_CONT(0.9) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc90)
                                             FROM View v
                                             WHERE v.page IS NOT NULL AND v.length IS NOT NULL
                                             GROUP BY v.page
                                             ORDER BY views DESC
                                             """, PageStats.class)
                                .getResultStream()
                                .toList();
        } else {
            return entityManager.createQuery("""
                                             SELECT new PageStats(v.page,
                                                                  COUNT(v.id) as views,
                                                                  AVG(v.length) as avgDuration,
                                                                  PERCENTILE_CONT(0.7) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc50,
                                                                  PERCENTILE_CONT(0.9) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc90)
                                             FROM View v
                                             WHERE v.page IS NOT NULL AND v.length IS NOT NULL AND v.accessTimestamp >= :start_date
                                             GROUP BY v.page
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

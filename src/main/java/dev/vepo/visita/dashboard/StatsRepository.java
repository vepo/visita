package dev.vepo.visita.dashboard;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class StatsRepository {
    private final EntityManager entityManager;

    @Inject
    public StatsRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<DailyStats> buildDailyViews(Selector selector, String parameter) {
        return switch (selector) {
            case REFERRER -> entityManager.createQuery("""
                                                       SELECT new DailyStats(DATE(v.accessTimestamp),
                                                                             COUNT(v.id),
                                                                             AVG(v.length),
                                                                             PERCENTILE_CONT(0.7) WITHIN GROUP (ORDER BY v.length),
                                                                             PERCENTILE_CONT(0.9) WITHIN GROUP (ORDER BY v.length))
                                                       FROM View v
                                                       WHERE v.referrer = :referrer AND v.accessTimestamp IS NOT NULL AND v.length IS NOT NULL
                                                       GROUP BY DATE(v.accessTimestamp)
                                                       ORDER BY DATE(v.accessTimestamp) DESC
                                                       """, DailyStats.class)
                                          .setParameter("referrer", parameter)
                                          .getResultStream()
                                          .toList();
            case DOMAIN -> entityManager.createQuery("""
                                                     SELECT new DailyStats(DATE(v.accessTimestamp),
                                                                           COUNT(v.id),
                                                                           AVG(v.length),
                                                                           PERCENTILE_CONT(0.7) WITHIN GROUP (ORDER BY v.length),
                                                                           PERCENTILE_CONT(0.9) WITHIN GROUP (ORDER BY v.length))
                                                     FROM View v
                                                     WHERE v.page.domain.hostname = :hostname AND v.accessTimestamp IS NOT NULL AND v.length IS NOT NULL
                                                     GROUP BY DATE(v.accessTimestamp)
                                                     ORDER BY DATE(v.accessTimestamp) DESC
                                                     """, DailyStats.class)
                                        .setParameter("hostname", parameter)
                                        .getResultStream()
                                        .toList();
            case NONE -> entityManager.createQuery("""
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
            default -> throw new UnsupportedOperationException("Selector not implemented! selector=%s".formatted(selector));
        };
    }

    public List<PageStats> findAllPageViews(Selector selector, String parameter) {
        return switch (selector) {
            case REFERRER -> entityManager.createQuery("""
                                                       SELECT new PageStats(v.page,
                                                                            COUNT(v.id) as views,
                                                                            AVG(v.length) as avgDuration,
                                                                            PERCENTILE_CONT(0.7) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc50,
                                                                            PERCENTILE_CONT(0.9) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc90)
                                                       FROM View v
                                                       WHERE v.page IS NOT NULL AND v.referrer = :referrer AND v.length IS NOT NULL
                                                       GROUP BY v.page
                                                       ORDER BY views DESC
                                                       """, PageStats.class)
                                          .setParameter("referrer", parameter)
                                          .getResultStream()
                                          .toList();
            case DOMAIN -> entityManager.createQuery("""
                                                     SELECT new PageStats(v.page,
                                                                          COUNT(v.id) as views,
                                                                          AVG(v.length) as avgDuration,
                                                                          PERCENTILE_CONT(0.7) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc50,
                                                                          PERCENTILE_CONT(0.9) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc90)
                                                     FROM View v
                                                     WHERE v.page IS NOT NULL AND v.page.domain.hostname = :hostname AND v.length IS NOT NULL
                                                     GROUP BY v.page
                                                     ORDER BY views DESC
                                                     """, PageStats.class)
                                        .setParameter("hostname", parameter)
                                        .getResultStream()
                                        .toList();
            case NONE -> entityManager.createQuery("""
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
            default -> throw new UnsupportedOperationException("Selector not implemented! selector=%s".formatted(selector));
        };
    }

    public List<PageStats> findPageViewsFromDate(Selector selector, String parameter, LocalDateTime startDate) {
        return switch (selector) {
            case DOMAIN -> entityManager.createQuery("""
                                                     SELECT new PageStats(v.page,
                                                                          COUNT(v.id) as views,
                                                                          AVG(v.length) as avgDuration,
                                                                          PERCENTILE_CONT(0.7) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc50,
                                                                          PERCENTILE_CONT(0.9) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc90)
                                                     FROM View v
                                                     WHERE v.page IS NOT NULL AND v.page.domain.hostname = :hostname AND v.length IS NOT NULL AND v.accessTimestamp >= :start_date
                                                     GROUP BY v.page
                                                     ORDER BY views DESC
                                                     """,
                                                     PageStats.class)
                                        .setParameter("start_date", startDate)
                                        .setParameter("hostname", parameter)
                                        .getResultStream()
                                        .toList();
            case REFERRER -> entityManager.createQuery("""
                                                     SELECT new PageStats(v.page,
                                                                          COUNT(v.id) as views,
                                                                          AVG(v.length) as avgDuration,
                                                                          PERCENTILE_CONT(0.7) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc50,
                                                                          PERCENTILE_CONT(0.9) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc90)
                                                     FROM View v
                                                     WHERE v.page IS NOT NULL AND v.referrer = :referrer AND v.length IS NOT NULL AND v.accessTimestamp >= :start_date
                                                     GROUP BY v.page
                                                     ORDER BY views DESC
                                                     """,
                                                     PageStats.class)
                                        .setParameter("start_date", startDate)
                                        .setParameter("referrer", parameter)
                                        .getResultStream()
                                        .toList();
            case NONE -> entityManager.createQuery("""
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
            default -> throw new UnsupportedOperationException("Selector not implemented! selector=%s".formatted(selector));
        };
    }

    public List<ReferrerStats> findAllReferrerStats(Selector selector, String parameter) {
        return switch (selector) {
            case DOMAIN -> entityManager.createQuery("""
                                                     SELECT new ReferrerStats(v.referrer,
                                                                              COUNT(v.id) as views,
                                                                              AVG(v.length) as avgDuration,
                                                                              PERCENTILE_CONT(0.7) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc50,
                                                                              PERCENTILE_CONT(0.9) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc90)
                                                     FROM View v
                                                     WHERE v.referrer IS NOT NULL AND v.page.domain.hostname = :hostname AND v.length IS NOT NULL
                                                     GROUP BY v.referrer
                                                     ORDER BY views DESC
                                                     """,
                                                     ReferrerStats.class)
                                        .setParameter("hostname", parameter)
                                        .getResultStream()
                                        .toList();
            case REFERRER -> entityManager.createQuery("""
                                                       SELECT new ReferrerStats(v.referrer,
                                                                                COUNT(v.id) as views,
                                                                                AVG(v.length) as avgDuration,
                                                                                PERCENTILE_CONT(0.7) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc50,
                                                                                PERCENTILE_CONT(0.9) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc90)
                                                       FROM View v
                                                       WHERE v.referrer IS NOT NULL AND v.referrer = :referrer AND v.length IS NOT NULL
                                                       GROUP BY v.referrer
                                                       ORDER BY views DESC
                                                       """,
                                                       ReferrerStats.class)
                                          .setParameter("referrer", parameter)
                                          .getResultStream()
                                          .toList();
            case NONE -> entityManager.createQuery("""
                                                   SELECT new ReferrerStats(v.referrer,
                                                                            COUNT(v.id) as views,
                                                                            AVG(v.length) as avgDuration,
                                                                            PERCENTILE_CONT(0.7) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc50,
                                                                            PERCENTILE_CONT(0.9) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc90)
                                                   FROM View v
                                                   WHERE v.referrer IS NOT NULL AND v.length IS NOT NULL
                                                   GROUP BY v.referrer
                                                   ORDER BY views DESC
                                                   """,
                                                   ReferrerStats.class)
                                      .getResultStream()
                                      .toList();
            default -> throw new UnsupportedOperationException("Selector not implemented! selector=%s".formatted(selector));
        };
    }
}

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
                                                                                   PERCENTILE_DISC(0.7) WITHIN GROUP (ORDER BY v.length),
                                                                                   PERCENTILE_DISC(0.9) WITHIN GROUP (ORDER BY v.length))
                                                             FROM View v
                                                             WHERE v.originalView IS NOT NULL AND
                                                                   v.originalView.referer = :referer AND
                                                                   v.accessTimestamp IS NOT NULL AND v.length IS NOT NULL
                                                             GROUP BY DATE(v.accessTimestamp)
                                                             ORDER BY DATE(v.accessTimestamp) DESC
                                                             """, DailyStats.class)
                                                .setParameter("referer", parameter)
                                                .getResultStream()
                                                .toList();
                  case DOMAIN -> entityManager.createQuery("""
                                                           SELECT new DailyStats(DATE(v.accessTimestamp),
                                                                                 COUNT(v.id),
                                                                                 AVG(v.length),
                                                                                 PERCENTILE_DISC(0.7) WITHIN GROUP (ORDER BY v.length),
                                                                                 PERCENTILE_DISC(0.9) WITHIN GROUP (ORDER BY v.length))
                                                           FROM View v
                                                           WHERE v.page.domain.hostname = :hostname AND
                                                                 v.accessTimestamp IS NOT NULL AND v.length IS NOT NULL
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
                                                                               PERCENTILE_DISC(0.7) WITHIN GROUP (ORDER BY v.length),
                                                                               PERCENTILE_DISC(0.9) WITHIN GROUP (ORDER BY v.length))
                                                         FROM View v
                                                         WHERE v.accessTimestamp IS NOT NULL AND
                                                               v.length IS NOT NULL
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
                                                                                  PERCENTILE_DISC(0.7) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc50,
                                                                                  PERCENTILE_DISC(0.9) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc90)
                                                             FROM View v
                                                             WHERE v.page IS NOT NULL AND
                                                                   v.originalView IS NOT NULL AND
                                                                   v.originalView.referer = :referer AND v.length IS NOT NULL
                                                             GROUP BY v.page
                                                             ORDER BY views DESC
                                                             """, PageStats.class)
                                                .setParameter("referer", parameter)
                                                .getResultStream()
                                                .toList();
                  case DOMAIN -> entityManager.createQuery("""
                                                           SELECT new PageStats(v.page,
                                                                                COUNT(v.id) as views,
                                                                                AVG(v.length) as avgDuration,
                                                                                PERCENTILE_DISC(0.7) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc50,
                                                                                PERCENTILE_DISC(0.9) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc90)
                                                           FROM View v
                                                           WHERE v.page IS NOT NULL AND
                                                                 v.page.domain.hostname = :hostname AND
                                                                 v.length IS NOT NULL
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
                                                                              PERCENTILE_DISC(0.7) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc50,
                                                                              PERCENTILE_DISC(0.9) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc90)
                                                         FROM View v
                                                         WHERE v.page IS NOT NULL AND
                                                               v.length IS NOT NULL
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
                                                                                PERCENTILE_DISC(0.7) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc50,
                                                                                PERCENTILE_DISC(0.9) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc90)
                                                           FROM View v
                                                           WHERE v.page IS NOT NULL AND
                                                                 v.page.domain.hostname = :hostname AND
                                                                 v.length IS NOT NULL AND
                                                                 v.accessTimestamp >= :start_date
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
                                                                                  PERCENTILE_DISC(0.7) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc50,
                                                                                  PERCENTILE_DISC(0.9) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc90)
                                                             FROM View v
                                                             WHERE v.page IS NOT NULL AND
                                                                   v.originalView IS NOT NULL AND
                                                                   v.originalView.referer = :referer AND
                                                                   v.length IS NOT NULL AND
                                                                   v.accessTimestamp >= :start_date
                                                             GROUP BY v.page
                                                             ORDER BY views DESC
                                                             """,
                                                             PageStats.class)
                                                .setParameter("start_date", startDate)
                                                .setParameter("referer", parameter)
                                                .getResultStream()
                                                .toList();
                  case NONE -> entityManager.createQuery("""
                                                         SELECT new PageStats(v.page,
                                                                              COUNT(v.id) as views,
                                                                              AVG(v.length) as avgDuration,
                                                                              PERCENTILE_DISC(0.7) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc50,
                                                                              PERCENTILE_DISC(0.9) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc90)
                                                         FROM View v
                                                         WHERE v.page IS NOT NULL AND
                                                               v.length IS NOT NULL AND
                                                               v.accessTimestamp >= :start_date
                                                         GROUP BY v.page
                                                         ORDER BY views DESC
                                                         """, PageStats.class)
                                            .setParameter("start_date", startDate)
                                            .getResultStream()
                                            .toList();
                  default -> throw new UnsupportedOperationException("Selector not implemented! selector=%s".formatted(selector));
            };
      }

      public List<RefererStats> findAllRefererStats(Selector selector, String parameter) {
            return switch (selector) {
                  case DOMAIN -> entityManager.createQuery("""
                                                           SELECT new RefererStats(v.originalView.referer,
                                                               COUNT(v.id) as views,
                                                               AVG(v.length) as avgDuration,
                                                               PERCENTILE_DISC(0.7) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc50,
                                                                                    PERCENTILE_DISC(0.9) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc90)
                                                           FROM View v
                                                           WHERE v.referer IS NOT NULL AND
                                                                 v.page.domain.hostname = :hostname AND
                                                                 v.length IS NOT NULL
                                                           GROUP BY v.originalView.referer
                                                           ORDER BY views DESC
                                                           """,
                                                           RefererStats.class)
                                              .setParameter("hostname", parameter)
                                              .getResultStream()
                                              .toList();
                  case REFERRER -> entityManager.createQuery("""
                                                             SELECT new RefererStats(v.originalView.referer,
                                                                 COUNT(v.id) as views,
                                                                 AVG(v.length) as avgDuration,
                                                                 PERCENTILE_DISC(0.7) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc50,
                                                                                      PERCENTILE_DISC(0.9) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc90)
                                                             FROM View v
                                                             WHERE v.referer IS NOT NULL AND
                                                                   v.originalView.referer = :referer AND
                                                                   v.length IS NOT NULL
                                                             GROUP BY v.originalView.referer
                                                             ORDER BY views DESC
                                                             """,
                                                             RefererStats.class)
                                                .setParameter("referer", parameter)
                                                .getResultStream()
                                                .toList();
                  case NONE -> entityManager.createQuery("""
                                                         SELECT new RefererStats(v.originalView.referer,
                                                             COUNT(v.id) as views,
                                                             AVG(v.length) as avgDuration,
                                                             PERCENTILE_DISC(0.7) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc50,
                                                                                  PERCENTILE_DISC(0.9) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc90)
                                                         FROM View v
                                                         WHERE v.referer IS NOT NULL AND
                                                               v.originalView.referer IS NOT NULL AND
                                                               v.length IS NOT NULL
                                                         GROUP BY v.originalView.referer
                                                         ORDER BY views DESC
                                                         """,
                                                         RefererStats.class)
                                            .getResultStream()
                                            .toList();
                  default -> throw new UnsupportedOperationException("Selector not implemented! selector=%s".formatted(selector));
            };
      }

      @SuppressWarnings("unchecked")
      public List<UniqueUsersStats> findUniqueUsersByPeriod(Selector selector, String parameter) {
            return switch (selector) {
                  case DOMAIN -> entityManager.createNativeQuery("""
                                                                 WITH available_days AS (
                                                                     SELECT DATE(access_timestamp) as currentDay
                                                                     FROM tb_views
                                                                     WHERE access_timestamp IS NOT NULL
                                                                     GROUP BY DATE(access_timestamp)
                                                                 )
                                                                 SELECT currentDay,
                                                                        (SELECT COUNT(DISTINCT v.user_id) FROM tb_views v
                                                                                                          LEFT JOIN tb_pages p ON v.page_id = p.id
                                                                                                          LEFT JOIN tb_domains d ON p.domain_id = d.id
                                                                                                          WHERE d.hostname = :hostname AND
                                                                                                                v.access_timestamp <= currentDay + interval '1 day' AND
                                                                                                                v.access_timestamp >  currentDay) as dailyActiveUsers,
                                                                        (SELECT COUNT(DISTINCT v.user_id) FROM tb_views v
                                                                                                          LEFT JOIN tb_pages p ON v.page_id = p.id
                                                                                                          LEFT JOIN tb_domains d ON p.domain_id = d.id
                                                                                                          WHERE d.hostname = :hostname AND
                                                                                                                v.access_timestamp <= currentDay + interval '1 day' AND
                                                                                                                v.access_timestamp >  currentDay - interval '1 week') as weeklyActiveUsers,
                                                                        (SELECT COUNT(DISTINCT v.user_id) FROM tb_views v
                                                                                                          LEFT JOIN tb_pages p ON v.page_id = p.id
                                                                                                          LEFT JOIN tb_domains d ON p.domain_id = d.id
                                                                                                          WHERE d.hostname = :hostname AND
                                                                                                                v.access_timestamp <= currentDay + interval '1 day' AND
                                                                                                                v.access_timestamp >  currentDay - interval '1 month') as monthlyActiveUsers
                                                                 FROM available_days
                                                                 """,
                                                                 UniqueUsersStats.class)
                                              .setParameter("hostname", parameter)
                                              .getResultStream()
                                              .toList();
                  case REFERRER -> entityManager.createNativeQuery("""
                                                                   WITH available_days AS (
                                                                       SELECT DATE(access_timestamp) as currentDay
                                                                       FROM tb_views
                                                                       WHERE access_timestamp IS NOT NULL
                                                                       GROUP BY DATE(access_timestamp)
                                                                   )
                                                                   SELECT currentDay,
                                                                          (SELECT COUNT(DISTINCT user_id) FROM tb_views
                                                                                                          WHERE id IN (SELECT tvor.id 
                                                                                                                       FROM tb_views_original_referer tvor 
                                                                                                                       WHERE tvor.original_referer = :referer) AND
                                                                                                                access_timestamp <= currentDay + interval '1 day' AND
                                                                                                                access_timestamp >  currentDay) as dailyActiveUsers,
                                                                          (SELECT COUNT(DISTINCT user_id) FROM tb_views
                                                                                                          WHERE id IN (SELECT tvor.id 
                                                                                                                       FROM tb_views_original_referer tvor 
                                                                                                                       WHERE tvor.original_referer = :referer) AND
                                                                                                                access_timestamp <= currentDay + interval '1 day' AND
                                                                                                                access_timestamp >  currentDay - interval '1 week') as weeklyActiveUsers,
                                                                          (SELECT COUNT(DISTINCT user_id) FROM tb_views
                                                                                                          WHERE id IN (SELECT tvor.id 
                                                                                                                       FROM tb_views_original_referer tvor
                                                                                                                       WHERE tvor.original_referer = :referer) AND
                                                                                                                access_timestamp <= currentDay + interval '1 day' AND
                                                                                                                access_timestamp >  currentDay - interval '1 month') as monthlyActiveUsers
                                                                   FROM available_days
                                                                   """,
                                                                   UniqueUsersStats.class)
                                                .setParameter("referer", parameter)
                                                .getResultStream()
                                                .toList();
                  case NONE -> entityManager.createNativeQuery("""
                                                               WITH available_days AS (
                                                                   SELECT DATE(access_timestamp) as currentDay
                                                                   FROM tb_views
                                                                   WHERE access_timestamp IS NOT NULL
                                                                   GROUP BY DATE(access_timestamp)
                                                               )
                                                               SELECT currentDay,
                                                                      (SELECT COUNT(DISTINCT user_id) FROM tb_views
                                                                                                      WHERE access_timestamp <= currentDay + interval '1 day' AND
                                                                                                            access_timestamp >  currentDay) as dailyActiveUsers,
                                                                      (SELECT COUNT(DISTINCT user_id) FROM tb_views
                                                                                                      WHERE access_timestamp <= currentDay + interval '1 day' AND
                                                                                                            access_timestamp >  currentDay - interval '1 week') as weeklyActiveUsers,
                                                                      (SELECT COUNT(DISTINCT user_id) FROM tb_views
                                                                                                      WHERE access_timestamp <= currentDay + interval '1 day' AND
                                                                                                            access_timestamp >  currentDay - interval '1 month') as monthlyActiveUsers
                                                               FROM available_days
                                                               """,
                                                               UniqueUsersStats.class)
                                            .getResultStream()
                                            .toList();
                  default -> throw new UnsupportedOperationException("Selector not implemented! selector=%s".formatted(selector));
            };
      }

      @SuppressWarnings("unchecked")
      public List<UniqueUsersStats> findUniqueUsersByPeriodFromDate(Selector selector, String parameter, LocalDateTime startDate) {
            return switch (selector) {
                  case DOMAIN -> entityManager.createNativeQuery("""
                                                                 WITH available_days AS (
                                                                     SELECT DATE(access_timestamp) as currentDay
                                                                     FROM tb_views
                                                                     WHERE access_timestamp IS NOT NULL AND
                                                                           access_timestamp >= :start_date
                                                                     GROUP BY DATE(access_timestamp)
                                                                 )
                                                                 SELECT currentDay,
                                                                        (SELECT COUNT(DISTINCT v.user_id) FROM tb_views v
                                                                                                          LEFT JOIN tb_pages p ON v.page_id = p.id
                                                                                                          LEFT JOIN tb_domains d ON p.domain_id = d.id
                                                                                                          WHERE d.hostname = :hostname AND
                                                                                                                v.access_timestamp <= currentDay + interval '1 day' AND
                                                                                                                v.access_timestamp >  currentDay) as dailyActiveUsers,
                                                                        (SELECT COUNT(DISTINCT v.user_id) FROM tb_views v
                                                                                                          LEFT JOIN tb_pages p ON v.page_id = p.id
                                                                                                          LEFT JOIN tb_domains d ON p.domain_id = d.id
                                                                                                          WHERE d.hostname = :hostname AND
                                                                                                                v.access_timestamp <= currentDay + interval '1 day' AND
                                                                                                                v.access_timestamp >  currentDay - interval '1 week') as weeklyActiveUsers,
                                                                        (SELECT COUNT(DISTINCT v.user_id) FROM tb_views v
                                                                                                          LEFT JOIN tb_pages p ON v.page_id = p.id
                                                                                                          LEFT JOIN tb_domains d ON p.domain_id = d.id
                                                                                                          WHERE d.hostname = :hostname AND
                                                                                                                v.access_timestamp <= currentDay + interval '1 day' AND
                                                                                                                v.access_timestamp >  currentDay - interval '1 month') as monthlyActiveUsers
                                                                 FROM available_days
                                                                 """,
                                                                 UniqueUsersStats.class)
                                              .setParameter("hostname", parameter)
                                              .setParameter("start_date", startDate)
                                              .getResultStream()
                                              .toList();
                  case REFERRER -> entityManager.createNativeQuery("""
                                                                   WITH available_days AS (
                                                                       SELECT DATE(access_timestamp) as currentDay
                                                                       FROM tb_views
                                                                       WHERE access_timestamp IS NOT NULL AND
                                                                             access_timestamp >= :start_date
                                                                       GROUP BY DATE(access_timestamp)
                                                                   )
                                                                   SELECT currentDay,
                                                                          (SELECT COUNT(DISTINCT user_id) FROM tb_views
                                                                                                          WHERE id IN(SELECT id FROM tb_views_original_referer WHERE original_referer = :referer) AND
                                                                                                                access_timestamp <= currentDay + interval '1 day' AND
                                                                                                                access_timestamp >  currentDay) as dailyActiveUsers,
                                                                          (SELECT COUNT(DISTINCT user_id) FROM tb_views
                                                                                                          WHERE id IN(SELECT id FROM tb_views_original_referer WHERE original_referer = :referer) AND
                                                                                                                access_timestamp <= currentDay + interval '1 day' AND
                                                                                                                access_timestamp >  currentDay - interval '1 week') as weeklyActiveUsers,
                                                                          (SELECT COUNT(DISTINCT user_id) FROM tb_views
                                                                                                          WHERE id IN(SELECT id FROM tb_views_original_referer WHERE original_referer = :referer) AND
                                                                                                                access_timestamp <= currentDay + interval '1 day' AND
                                                                                                                access_timestamp >  currentDay - interval '1 month') as monthlyActiveUsers
                                                                   FROM available_days
                                                                   """,
                                                                   UniqueUsersStats.class)
                                                .setParameter("referer", parameter)
                                                .setParameter("start_date", startDate)
                                                .getResultStream()
                                                .toList();
                  case NONE -> entityManager.createNativeQuery("""
                                                               WITH available_days AS (
                                                                   SELECT DATE(access_timestamp) as currentDay
                                                                   FROM tb_views
                                                                   WHERE access_timestamp IS NOT NULL AND
                                                                         access_timestamp >= :start_date
                                                                   GROUP BY DATE(access_timestamp)
                                                               )
                                                               SELECT currentDay,
                                                                      (SELECT COUNT(DISTINCT user_id) FROM tb_views
                                                                                                      WHERE access_timestamp <= currentDay + interval '1 day' AND
                                                                                                            access_timestamp >  currentDay) as dailyActiveUsers,
                                                                      (SELECT COUNT(DISTINCT user_id) FROM tb_views
                                                                                                      WHERE access_timestamp <= currentDay + interval '1 day' AND
                                                                                                            access_timestamp >  currentDay - interval '1 week') as weeklyActiveUsers,
                                                                      (SELECT COUNT(DISTINCT user_id) FROM tb_views
                                                                                                      WHERE access_timestamp <= currentDay + interval '1 day' AND
                                                                                                            access_timestamp >  currentDay - interval '1 month') as monthlyActiveUsers
                                                               FROM available_days
                                                               """,
                                                               UniqueUsersStats.class)
                                            .setParameter("start_date", startDate)
                                            .getResultStream()
                                            .toList();
                  default -> throw new UnsupportedOperationException("Selector not implemented! selector=%s".formatted(selector));
            };
      }
}

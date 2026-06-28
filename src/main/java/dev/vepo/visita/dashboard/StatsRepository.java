package dev.vepo.visita.dashboard;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.vepo.visita.page.Page;
import dev.vepo.visita.stats.summary.DomainViewCount;
import dev.vepo.visita.stats.summary.PageViewCount;
import dev.vepo.visita.stats.summary.StatsSummary;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class StatsRepository {
    private static final Logger logger = LoggerFactory.getLogger(StatsRepository.class);
    private static final String EXCLUDE_IGNORED_PAGES = "v.page.id NOT IN :excludedPageIds";
    private static final Set<Long> NO_EXCLUDED_PAGES = Set.of(-1L);
    private static final int REFERRER_PAGE_FLOW_LIMIT = 30;

    private final EntityManager entityManager;

    @Inject
    public StatsRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<DailyStats> buildDailyViews(Selector selector, String parameter, LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Building daily stats {}={}, startDate={}, endDate={}", selector, parameter, startDate, endDate);
        var excludedPageIds = excludedPageIdsParameter();
        return switch (selector) {
            case REFERRER -> entityManager.createQuery("""
                                                       SELECT new DailyStats(DATE(v.accessTimestamp),
                                                                             COUNT(v.id),
                                                                             AVG(v.length),
                                                                             PERCENTILE_DISC(0.7) WITHIN GROUP (ORDER BY v.length),
                                                                             PERCENTILE_DISC(0.9) WITHIN GROUP (ORDER BY v.length))
                                                       FROM View v
                                                       WHERE (v.originalReferrer = :referrer OR v.referrer = :referrer) AND
                                                             v.accessTimestamp IS NOT NULL AND
                                                             v.length IS NOT NULL AND
                                                             %s AND
                                                             (COALESCE(:startDate, NULL) IS NULL OR v.accessTimestamp >= :startDate) AND
                                                             (COALESCE(:endDate, NULL) IS NULL OR v.accessTimestamp < :endDate)
                                                       GROUP BY DATE(v.accessTimestamp)
                                                       ORDER BY DATE(v.accessTimestamp) DESC
                                                       """.formatted(EXCLUDE_IGNORED_PAGES), DailyStats.class)
                                          .setParameter("referrer", parameter)
                                          .setParameter("excludedPageIds", excludedPageIds)
                                          .setParameter("startDate", startDate)
                                          .setParameter("endDate", endDate)
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
                                                           v.accessTimestamp IS NOT NULL AND v.length IS NOT NULL AND
                                                           %s AND
                                                           (COALESCE(:startDate, NULL) IS NULL OR v.accessTimestamp >= :startDate) AND
                                                           (COALESCE(:endDate, NULL) IS NULL OR v.accessTimestamp < :endDate)
                                                     GROUP BY DATE(v.accessTimestamp)
                                                     ORDER BY DATE(v.accessTimestamp) DESC
                                                     """.formatted(EXCLUDE_IGNORED_PAGES), DailyStats.class)
                                        .setParameter("hostname", parameter)
                                        .setParameter("excludedPageIds", excludedPageIds)
                                        .setParameter("startDate", startDate)
                                        .setParameter("endDate", endDate)
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
                                                         v.length IS NOT NULL AND
                                                         %s AND
                                                         (COALESCE(:startDate, NULL) IS NULL OR v.accessTimestamp >= :startDate) AND
                                                         (COALESCE(:endDate, NULL) IS NULL OR v.accessTimestamp < :endDate)
                                                   GROUP BY DATE(v.accessTimestamp)
                                                   ORDER BY DATE(v.accessTimestamp) DESC
                                                   """.formatted(EXCLUDE_IGNORED_PAGES), DailyStats.class)
                                      .setParameter("excludedPageIds", excludedPageIds)
                                      .setParameter("startDate", startDate)
                                      .setParameter("endDate", endDate)
                                      .getResultStream()
                                      .toList();
            default -> throw new UnsupportedOperationException("Selector not implemented! selector=%s".formatted(selector));
        };
    }

    public List<PageStats> buildPageViews(Selector selector, String parameter, LocalDateTime startDate, LocalDateTime endDate) {
        var excludedPageIds = excludedPageIdsParameter();
        return switch (selector) {
            case REFERRER -> entityManager.createQuery("""
                                                       SELECT new PageStats(v.page,
                                                                            COUNT(v.id) as views,
                                                                            AVG(v.length) as avgDuration,
                                                                            PERCENTILE_DISC(0.7) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc50,
                                                                            PERCENTILE_DISC(0.9) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc90)
                                                       FROM View v
                                                       WHERE v.page IS NOT NULL AND
                                                             (v.originalReferrer = :referrer OR v.referrer = :referrer) AND
                                                             v.length IS NOT NULL AND
                                                             %s AND
                                                             (COALESCE(:startDate, NULL) IS NULL OR v.accessTimestamp >= :startDate) AND
                                                             (COALESCE(:endDate, NULL) IS NULL OR v.accessTimestamp < :endDate)
                                                       GROUP BY v.page
                                                       ORDER BY views DESC
                                                       """.formatted(EXCLUDE_IGNORED_PAGES), PageStats.class)
                                          .setParameter("referrer", parameter)
                                          .setParameter("excludedPageIds", excludedPageIds)
                                          .setParameter("startDate", startDate)
                                          .setParameter("endDate", endDate)
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
                                                           v.length IS NOT NULL AND
                                                           %s AND
                                                           (COALESCE(:startDate, NULL) IS NULL OR v.accessTimestamp >= :startDate) AND
                                                           (COALESCE(:endDate, NULL) IS NULL OR v.accessTimestamp < :endDate)
                                                     GROUP BY v.page
                                                     ORDER BY views DESC
                                                     """.formatted(EXCLUDE_IGNORED_PAGES), PageStats.class)
                                        .setParameter("hostname", parameter)
                                        .setParameter("excludedPageIds", excludedPageIds)
                                        .setParameter("startDate", startDate)
                                        .setParameter("endDate", endDate)
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
                                                         %s AND
                                                         (COALESCE(:startDate, NULL) IS NULL OR v.accessTimestamp >= :startDate) AND
                                                         (COALESCE(:endDate, NULL) IS NULL OR v.accessTimestamp < :endDate)
                                                   GROUP BY v.page
                                                   ORDER BY views DESC
                                                   """.formatted(EXCLUDE_IGNORED_PAGES), PageStats.class)
                                      .setParameter("excludedPageIds", excludedPageIds)
                                      .setParameter("startDate", startDate)
                                      .setParameter("endDate", endDate)
                                      .getResultStream()
                                      .toList();
            default -> throw new UnsupportedOperationException("Selector not implemented! selector=%s".formatted(selector));
        };
    }

    public List<PageStats> buildPageViewsFromDate(Selector selector, String parameter, LocalDateTime startDate) {
        var excludedPageIds = excludedPageIdsParameter();
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
                                                           %s AND
                                                           v.accessTimestamp >= :start_date
                                                     GROUP BY v.page
                                                     ORDER BY views DESC
                                                     """.formatted(EXCLUDE_IGNORED_PAGES),
                                                     PageStats.class)
                                        .setParameter("start_date", startDate)
                                        .setParameter("hostname", parameter)
                                        .setParameter("excludedPageIds", excludedPageIds)
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
                                                             (v.originalReferrer = :referrer OR v.referrer = :referrer) AND
                                                             v.length IS NOT NULL AND
                                                             %s AND
                                                             v.accessTimestamp >= :start_date
                                                       GROUP BY v.page
                                                       ORDER BY views DESC
                                                       """.formatted(EXCLUDE_IGNORED_PAGES),
                                                       PageStats.class)
                                          .setParameter("start_date", startDate)
                                          .setParameter("referrer", parameter)
                                          .setParameter("excludedPageIds", excludedPageIds)
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
                                                         %s AND
                                                         v.accessTimestamp >= :start_date
                                                   GROUP BY v.page
                                                   ORDER BY views DESC
                                                   """.formatted(EXCLUDE_IGNORED_PAGES), PageStats.class)
                                      .setParameter("start_date", startDate)
                                      .setParameter("excludedPageIds", excludedPageIds)
                                      .getResultStream()
                                      .toList();
            default -> throw new UnsupportedOperationException("Selector not implemented! selector=%s".formatted(selector));
        };
    }

    public List<ReferrerStats> buildReferrerStats(Selector selector, String parameter, LocalDateTime startDate, LocalDateTime endDate) {
        var excludedPageIds = excludedPageIdsParameter();
        return switch (selector) {
            case DOMAIN -> entityManager.createQuery("""
                                                     SELECT new ReferrerStats(COALESCE(v.originalReferrer, v.referrer),
                                                         COUNT(v.id) as views,
                                                         AVG(v.length) as avgDuration,
                                                         PERCENTILE_DISC(0.7) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc50,
                                                         PERCENTILE_DISC(0.9) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc90)
                                                     FROM View v
                                                     WHERE (v.originalReferrer IS NOT NULL OR v.referrer IS NOT NULL) AND
                                                           v.page.domain.hostname = :hostname AND
                                                           v.length IS NOT NULL AND
                                                           %s AND
                                                           (COALESCE(:startDate, NULL) IS NULL OR v.accessTimestamp >= :startDate) AND
                                                           (COALESCE(:endDate, NULL) IS NULL OR v.accessTimestamp < :endDate)
                                                     GROUP BY COALESCE(v.originalReferrer, v.referrer)
                                                     ORDER BY views DESC
                                                     """.formatted(EXCLUDE_IGNORED_PAGES),
                                                     ReferrerStats.class)
                                        .setParameter("hostname", parameter)
                                        .setParameter("excludedPageIds", excludedPageIds)
                                        .setParameter("startDate", startDate)
                                        .setParameter("endDate", endDate)
                                        .getResultStream()
                                        .toList();
            case REFERRER -> entityManager.createQuery("""
                                                       SELECT new ReferrerStats(COALESCE(v.originalReferrer, v.referrer),
                                                           COUNT(v.id) as views,
                                                           AVG(v.length) as avgDuration,
                                                           PERCENTILE_DISC(0.7) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc50,
                                                           PERCENTILE_DISC(0.9) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc90)
                                                       FROM View v
                                                       WHERE (v.originalReferrer = :referrer OR v.referrer = :referrer) AND
                                                             v.length IS NOT NULL AND
                                                             %s AND
                                                             (COALESCE(:startDate, NULL) IS NULL OR v.accessTimestamp >= :startDate) AND
                                                             (COALESCE(:endDate, NULL) IS NULL OR v.accessTimestamp < :endDate)
                                                       GROUP BY COALESCE(v.originalReferrer, v.referrer)
                                                       ORDER BY views DESC
                                                       """.formatted(EXCLUDE_IGNORED_PAGES),
                                                       ReferrerStats.class)
                                          .setParameter("referrer", parameter)
                                          .setParameter("excludedPageIds", excludedPageIds)
                                          .setParameter("startDate", startDate)
                                          .setParameter("endDate", endDate)
                                          .getResultStream()
                                          .toList();
            case NONE -> entityManager.createQuery("""
                                                   SELECT new ReferrerStats(COALESCE(v.originalReferrer, v.referrer),
                                                       COUNT(v.id) as views,
                                                       AVG(v.length) as avgDuration,
                                                       PERCENTILE_DISC(0.7) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc50,
                                                       PERCENTILE_DISC(0.9) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc90)
                                                   FROM View v
                                                   WHERE (v.originalReferrer IS NOT NULL OR v.referrer IS NOT NULL) AND
                                                         v.length IS NOT NULL AND
                                                         %s AND
                                                         (COALESCE(:startDate, NULL) IS NULL OR v.accessTimestamp >= :startDate) AND
                                                         (COALESCE(:endDate, NULL) IS NULL OR v.accessTimestamp < :endDate)
                                                   GROUP BY COALESCE(v.originalReferrer, v.referrer)
                                                   ORDER BY views DESC
                                                   """.formatted(EXCLUDE_IGNORED_PAGES),
                                                   ReferrerStats.class)
                                      .setParameter("excludedPageIds", excludedPageIds)
                                      .setParameter("startDate", startDate)
                                      .setParameter("endDate", endDate)
                                      .getResultStream()
                                      .toList();
            default -> throw new UnsupportedOperationException("Selector not implemented! selector=%s".formatted(selector));
        };
    }

    public List<DomainStats> buildDomainStats(Selector selector, String parameter, LocalDateTime startDate, LocalDateTime endDate) {
        var excludedPageIds = excludedPageIdsParameter();
        return switch (selector) {
            case DOMAIN -> entityManager.createQuery("""
                                                     SELECT new DomainStats(v.page.domain.hostname,
                                                         COUNT(v.id) as views,
                                                         AVG(v.length) as avgDuration,
                                                         PERCENTILE_DISC(0.7) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc50,
                                                         PERCENTILE_DISC(0.9) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc90)
                                                     FROM View v
                                                     WHERE v.page.domain.hostname = :hostname AND
                                                           v.length IS NOT NULL AND
                                                           %s AND
                                                           (COALESCE(:startDate, NULL) IS NULL OR v.accessTimestamp >= :startDate) AND
                                                           (COALESCE(:endDate, NULL) IS NULL OR v.accessTimestamp < :endDate)
                                                     GROUP BY v.page.domain.hostname
                                                     ORDER BY views DESC
                                                     """.formatted(EXCLUDE_IGNORED_PAGES),
                                                     DomainStats.class)
                                        .setParameter("hostname", parameter)
                                        .setParameter("excludedPageIds", excludedPageIds)
                                        .setParameter("startDate", startDate)
                                        .setParameter("endDate", endDate)
                                        .getResultStream()
                                        .toList();
            case REFERRER -> entityManager.createQuery("""
                                                       SELECT new DomainStats(v.page.domain.hostname,
                                                           COUNT(v.id) as views,
                                                           AVG(v.length) as avgDuration,
                                                           PERCENTILE_DISC(0.7) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc50,
                                                           PERCENTILE_DISC(0.9) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc90)
                                                       FROM View v
                                                       WHERE (v.originalReferrer = :referrer OR v.referrer = :referrer) AND
                                                             v.length IS NOT NULL AND
                                                             %s AND
                                                             (COALESCE(:startDate, NULL) IS NULL OR v.accessTimestamp >= :startDate) AND
                                                             (COALESCE(:endDate, NULL) IS NULL OR v.accessTimestamp < :endDate)
                                                       GROUP BY v.page.domain.hostname
                                                       ORDER BY views DESC
                                                       """.formatted(EXCLUDE_IGNORED_PAGES),
                                                       DomainStats.class)
                                          .setParameter("referrer", parameter)
                                          .setParameter("excludedPageIds", excludedPageIds)
                                          .setParameter("startDate", startDate)
                                          .setParameter("endDate", endDate)
                                          .getResultStream()
                                          .toList();
            case NONE -> entityManager.createQuery("""
                                                   SELECT new DomainStats(v.page.domain.hostname,
                                                       COUNT(v.id) as views,
                                                       AVG(v.length) as avgDuration,
                                                       PERCENTILE_DISC(0.7) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc50,
                                                       PERCENTILE_DISC(0.9) WITHIN GROUP (ORDER BY v.length) as avgDurationPerc90)
                                                   FROM View v
                                                   WHERE v.length IS NOT NULL AND
                                                         %s AND
                                                         (COALESCE(:startDate, NULL) IS NULL OR v.accessTimestamp >= :startDate) AND
                                                         (COALESCE(:endDate, NULL) IS NULL OR v.accessTimestamp < :endDate)
                                                   GROUP BY v.page.domain.hostname
                                                   ORDER BY views DESC
                                                   """.formatted(EXCLUDE_IGNORED_PAGES),
                                                   DomainStats.class)
                                      .setParameter("excludedPageIds", excludedPageIds)
                                      .setParameter("startDate", startDate)
                                      .setParameter("endDate", endDate)
                                      .getResultStream()
                                      .toList();
            default -> throw new UnsupportedOperationException("Selector not implemented! selector=%s".formatted(selector));
        };
    }

    @SuppressWarnings("unchecked")
    public List<UniqueUsersStats> buildUniqueViews(Selector selector, String parameter, LocalDateTime startDate, LocalDateTime endDate) {
        var excludedPageIds = excludedPageIdsParameter();
        return switch (selector) {
            case DOMAIN -> entityManager.createNativeQuery("""
                                                           WITH available_days AS (
                                                               SELECT DATE(v.access_timestamp) as currentDay
                                                               FROM tb_views v
                                                               LEFT JOIN tb_pages p ON v.page_id = p.id
                                                               WHERE access_timestamp IS NOT NULL AND
                                                                     COALESCE(p.id, -1) NOT IN (:excludedPageIds) AND
                                                                     (COALESCE(:startDate, NULL) IS NULL OR access_timestamp >= :startDate) AND
                                                                     (COALESCE(:endDate, NULL) IS NULL OR access_timestamp < :endDate)
                                                               GROUP BY DATE(v.access_timestamp)
                                                           )
                                                           SELECT currentDay,
                                                                  (SELECT COUNT(DISTINCT v.user_id) FROM tb_views v
                                                                                                    LEFT JOIN tb_pages p ON v.page_id = p.id
                                                                                                    LEFT JOIN tb_domains d ON p.domain_id = d.id
                                                                                                    WHERE d.hostname = :hostname AND
                                                                                                          COALESCE(p.id, -1) NOT IN (:excludedPageIds) AND
                                                                                                          v.access_timestamp <= currentDay + interval '1 day' AND
                                                                                                          v.access_timestamp >  currentDay) as dailyActiveUsers,
                                                                  (SELECT COUNT(DISTINCT v.user_id) FROM tb_views v
                                                                                                    LEFT JOIN tb_pages p ON v.page_id = p.id
                                                                                                    LEFT JOIN tb_domains d ON p.domain_id = d.id
                                                                                                    WHERE d.hostname = :hostname AND
                                                                                                          COALESCE(p.id, -1) NOT IN (:excludedPageIds) AND
                                                                                                          v.access_timestamp <= currentDay + interval '1 day' AND
                                                                                                          v.access_timestamp >  currentDay - interval '1 week') as weeklyActiveUsers,
                                                                  (SELECT COUNT(DISTINCT v.user_id) FROM tb_views v
                                                                                                    LEFT JOIN tb_pages p ON v.page_id = p.id
                                                                                                    LEFT JOIN tb_domains d ON p.domain_id = d.id
                                                                                                    WHERE d.hostname = :hostname AND
                                                                                                          COALESCE(p.id, -1) NOT IN (:excludedPageIds) AND
                                                                                                          v.access_timestamp <= currentDay + interval '1 day' AND
                                                                                                          v.access_timestamp >  currentDay - interval '1 month') as monthlyActiveUsers
                                                           FROM available_days
                                                           """,
                                                           UniqueUsersStats.class)
                                        .setParameter("hostname", parameter)
                                        .setParameter("excludedPageIds", excludedPageIds)
                                        .setParameter("startDate", startDate)
                                        .setParameter("endDate", endDate)
                                        .getResultStream()
                                        .toList();
            case REFERRER -> entityManager.createNativeQuery("""
                                                             WITH available_days AS (
                                                                 SELECT DATE(v.access_timestamp) as currentDay
                                                                 FROM tb_views v
                                                                 LEFT JOIN tb_pages p ON v.page_id = p.id
                                                                 WHERE access_timestamp IS NOT NULL AND
                                                                       COALESCE(p.id, -1) NOT IN (:excludedPageIds) AND
                                                                       (COALESCE(:startDate, NULL) IS NULL OR access_timestamp >= :startDate) AND
                                                                       (COALESCE(:endDate, NULL) IS NULL OR access_timestamp < :endDate)
                                                                 GROUP BY DATE(v.access_timestamp)
                                                             )
                                                             SELECT currentDay,
                                                                    (SELECT COUNT(DISTINCT user_id) FROM tb_views v
                                                                                                    LEFT JOIN tb_pages p ON v.page_id = p.id
                                                                                                    WHERE (referrer = :referrer OR original_referrer = :referrer) AND
                                                                                                          COALESCE(p.id, -1) NOT IN (:excludedPageIds) AND
                                                                                                          access_timestamp <= currentDay + interval '1 day' AND
                                                                                                          access_timestamp >  currentDay) as dailyActiveUsers,
                                                                    (SELECT COUNT(DISTINCT user_id) FROM tb_views v
                                                                                                    LEFT JOIN tb_pages p ON v.page_id = p.id
                                                                                                    WHERE (referrer = :referrer OR original_referrer = :referrer) AND
                                                                                                          COALESCE(p.id, -1) NOT IN (:excludedPageIds) AND
                                                                                                          access_timestamp <= currentDay + interval '1 day' AND
                                                                                                          access_timestamp >  currentDay - interval '1 week') as weeklyActiveUsers,
                                                                    (SELECT COUNT(DISTINCT user_id) FROM tb_views v
                                                                                                    LEFT JOIN tb_pages p ON v.page_id = p.id
                                                                                                    WHERE (referrer = :referrer OR original_referrer = :referrer) AND
                                                                                                          COALESCE(p.id, -1) NOT IN (:excludedPageIds) AND
                                                                                                          access_timestamp <= currentDay + interval '1 day' AND
                                                                                                          access_timestamp >  currentDay - interval '1 month') as monthlyActiveUsers
                                                             FROM available_days
                                                             """,
                                                             UniqueUsersStats.class)
                                          .setParameter("referrer", parameter)
                                          .setParameter("excludedPageIds", excludedPageIds)
                                          .setParameter("startDate", startDate)
                                          .setParameter("endDate", endDate)
                                          .getResultStream()
                                          .toList();
            case NONE -> entityManager.createNativeQuery("""
                                                         WITH available_days AS (
                                                             SELECT DATE(v.access_timestamp) as currentDay
                                                             FROM tb_views v
                                                             LEFT JOIN tb_pages p ON v.page_id = p.id
                                                             WHERE access_timestamp IS NOT NULL AND
                                                                   COALESCE(p.id, -1) NOT IN (:excludedPageIds) AND
                                                                   (COALESCE(:startDate, NULL) IS NULL OR access_timestamp >= :startDate) AND
                                                                   (COALESCE(:endDate, NULL) IS NULL OR access_timestamp < :endDate)
                                                             GROUP BY DATE(v.access_timestamp)
                                                         )
                                                         SELECT currentDay,
                                                                (SELECT COUNT(DISTINCT user_id) FROM tb_views v
                                                                                                LEFT JOIN tb_pages p ON v.page_id = p.id
                                                                                                WHERE COALESCE(p.id, -1) NOT IN (:excludedPageIds) AND
                                                                                                      access_timestamp <= currentDay + interval '1 day' AND
                                                                                                      access_timestamp >  currentDay) as dailyActiveUsers,
                                                                (SELECT COUNT(DISTINCT user_id) FROM tb_views v
                                                                                                LEFT JOIN tb_pages p ON v.page_id = p.id
                                                                                                WHERE COALESCE(p.id, -1) NOT IN (:excludedPageIds) AND
                                                                                                      access_timestamp <= currentDay + interval '1 day' AND
                                                                                                      access_timestamp >  currentDay - interval '1 week') as weeklyActiveUsers,
                                                                (SELECT COUNT(DISTINCT user_id) FROM tb_views v
                                                                                                LEFT JOIN tb_pages p ON v.page_id = p.id
                                                                                                WHERE COALESCE(p.id, -1) NOT IN (:excludedPageIds) AND
                                                                                                      access_timestamp <= currentDay + interval '1 day' AND
                                                                                                      access_timestamp >  currentDay - interval '1 month') as monthlyActiveUsers
                                                         FROM available_days
                                                         """,
                                                         UniqueUsersStats.class)
                                      .setParameter("excludedPageIds", excludedPageIds)
                                      .setParameter("startDate", startDate)
                                      .setParameter("endDate", endDate)
                                      .getResultStream()
                                      .toList();
            default -> throw new UnsupportedOperationException("Selector not implemented! selector=%s".formatted(selector));
        };
    }

    public List<ReferrerPageFlow> buildReferrerPageFlows(Selector selector,
                                                         String parameter,
                                                         LocalDateTime startDate,
                                                         LocalDateTime endDate) {
        var excludedPageIds = excludedPageIdsParameter();
        var flows = switch (selector) {
            case REFERRER -> entityManager.createQuery("""
                                                       SELECT new ReferrerPageFlow(COALESCE(v.originalReferrer, v.referrer),
                                                                                   v.page,
                                                                                   COUNT(v.id))
                                                       FROM View v
                                                       WHERE v.page IS NOT NULL AND
                                                             (v.originalReferrer = :referrer OR v.referrer = :referrer) AND
                                                             v.length IS NOT NULL AND
                                                             %s AND
                                                             (COALESCE(:startDate, NULL) IS NULL OR v.accessTimestamp >= :startDate) AND
                                                             (COALESCE(:endDate, NULL) IS NULL OR v.accessTimestamp < :endDate)
                                                       GROUP BY COALESCE(v.originalReferrer, v.referrer), v.page
                                                       ORDER BY COUNT(v.id) DESC
                                                       """.formatted(EXCLUDE_IGNORED_PAGES), ReferrerPageFlow.class)
                                          .setParameter("referrer", parameter)
                                          .setParameter("excludedPageIds", excludedPageIds)
                                          .setParameter("startDate", startDate)
                                          .setParameter("endDate", endDate)
                                          .getResultStream()
                                          .toList();
            case DOMAIN -> entityManager.createQuery("""
                                                     SELECT new ReferrerPageFlow(COALESCE(v.originalReferrer, v.referrer),
                                                                                 v.page,
                                                                                 COUNT(v.id))
                                                     FROM View v
                                                     WHERE v.page IS NOT NULL AND
                                                           v.page.domain.hostname = :hostname AND
                                                           v.length IS NOT NULL AND
                                                           %s AND
                                                           (COALESCE(:startDate, NULL) IS NULL OR v.accessTimestamp >= :startDate) AND
                                                           (COALESCE(:endDate, NULL) IS NULL OR v.accessTimestamp < :endDate)
                                                     GROUP BY COALESCE(v.originalReferrer, v.referrer), v.page
                                                     ORDER BY COUNT(v.id) DESC
                                                     """.formatted(EXCLUDE_IGNORED_PAGES), ReferrerPageFlow.class)
                                        .setParameter("hostname", parameter)
                                        .setParameter("excludedPageIds", excludedPageIds)
                                        .setParameter("startDate", startDate)
                                        .setParameter("endDate", endDate)
                                        .getResultStream()
                                        .toList();
            case NONE -> entityManager.createQuery("""
                                                   SELECT new ReferrerPageFlow(COALESCE(v.originalReferrer, v.referrer),
                                                                               v.page,
                                                                               COUNT(v.id))
                                                   FROM View v
                                                   WHERE v.page IS NOT NULL AND
                                                         v.length IS NOT NULL AND
                                                         %s AND
                                                         (COALESCE(:startDate, NULL) IS NULL OR v.accessTimestamp >= :startDate) AND
                                                         (COALESCE(:endDate, NULL) IS NULL OR v.accessTimestamp < :endDate)
                                                   GROUP BY COALESCE(v.originalReferrer, v.referrer), v.page
                                                   ORDER BY COUNT(v.id) DESC
                                                   """.formatted(EXCLUDE_IGNORED_PAGES), ReferrerPageFlow.class)
                                      .setParameter("excludedPageIds", excludedPageIds)
                                      .setParameter("startDate", startDate)
                                      .setParameter("endDate", endDate)
                                      .getResultStream()
                                      .toList();
            default -> throw new UnsupportedOperationException("Selector not implemented! selector=%s".formatted(selector));
        };
        return flows.stream().limit(REFERRER_PAGE_FLOW_LIMIT).toList();
    }

    public StatsSummary buildStatsSummary(Selector selector,
                                          String parameter,
                                          LocalDateTime startDate,
                                          LocalDateTime endDate,
                                          int topDomainsLimit,
                                          int topPagesLimit) {
        var dailyViews = buildDailyViews(selector, parameter, startDate, endDate);
        var pageViews = buildPageViews(selector, parameter, startDate, endDate);
        var topDomains = buildDomainStats(selector, parameter, startDate, endDate)
                                                                                  .stream()
                                                                                  .limit(topDomainsLimit)
                                                                                  .map(stats -> new DomainViewCount(stats.domain(), stats.views()))
                                                                                  .toList();
        var topPagesLastWeek = buildPageViewsFromDate(selector, parameter, LocalDateTime.now().minusDays(7))
                                                                                                            .stream()
                                                                                                            .limit(topPagesLimit)
                                                                                                            .map(stats -> new PageViewCount(stats.page(),
                                                                                                                                            stats.views()))
                                                                                                            .toList();
        var totalViews = dailyViews.stream().mapToLong(DailyStats::views).sum();

        return new StatsSummary(totalViews, dailyViews.size(), pageViews.size(), topDomains, topPagesLastWeek);
    }

    private Set<Long> excludedPageIdsParameter() {
        var ignoredPageIds = entityManager.createQuery("""
                                                       SELECT p FROM Page p JOIN FETCH p.domain d
                                                       WHERE d.ignoredPathPatterns IS NOT NULL AND TRIM(d.ignoredPathPatterns) <> ''
                                                       """, Page.class)
                                          .getResultStream()
                                          .filter(page -> page.getDomain().ignoresPath(page.getPath()))
                                          .map(Page::getId)
                                          .collect(Collectors.toSet());
        if (ignoredPageIds.isEmpty()) {
            return NO_EXCLUDED_PAGES;
        }
        return ignoredPageIds;
    }
}
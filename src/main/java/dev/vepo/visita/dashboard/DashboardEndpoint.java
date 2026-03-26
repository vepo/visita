package dev.vepo.visita.dashboard;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/dashboard")
public class DashboardEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(DashboardEndpoint.class);

    private static final String TOTAL_VIEWS = "totalViews";
    private static final String PAGE_VIEWS_LAST_WEEK = "pageViewsLastWeek";
    private static final String PAGE_VIEWS = "pageViews";
    private static final String REFERER_VIEWS = "referrerViews";
    private static final String DOMAIN_VIEWS = "domainViews";
    private static final String UNIQUE_VIEWS = "uniqueViews";
    private static final String DAILY_VIEWS = "dailyViews";

    private final Template dashboard;

    private final StatsRepository statsRepository;

    @Inject
    public DashboardEndpoint(StatsRepository statsRepository, Template dashboard) {
        this.statsRepository = statsRepository;
        this.dashboard = dashboard;
    }

    @GET
    @Operation(hidden = true)
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance render(@QueryParam("startDate") LocalDate startDate,
                                   @QueryParam("endDate") LocalDate endDate) {
        return load(Selector.NONE, null, toDateTime(startDate, true), toDateTime(endDate, false));
    }

    @GET
    @Operation(hidden = true)
    @Path("/domain/{domain}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance renderDomain(@PathParam("domain") String domain,
                                         @QueryParam("startDate") LocalDate startDate,
                                         @QueryParam("endDate") LocalDate endDate) {
        return load(Selector.DOMAIN, domain, toDateTime(startDate, true), toDateTime(endDate, false));
    }

    @GET
    @Operation(hidden = true)
    @Path("/referrer/{referrer}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance renderReferrer(@PathParam("referrer") String referrer,
                                           @QueryParam("startDate") LocalDate startDate,
                                           @QueryParam("endDate") LocalDate endDate) {
        return load(Selector.REFERRER, referrer, toDateTime(startDate, true), toDateTime(endDate, false));
    }

    private LocalDateTime toDateTime(LocalDate date, boolean upper) {
        if (Objects.isNull(date)) {
            return null;
        }
        if (upper) {
            return date.atStartOfDay();
        } else {
            return date.plusDays(1).atStartOfDay();
        }
    }

    private TemplateInstance load(Selector selector, String parameter, LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Loading dashboard for: {}={} startDate={}", selector, parameter, startDate);
        var dailyViews = statsRepository.buildDailyViews(selector, parameter, startDate, endDate);
        return dashboard.data(DAILY_VIEWS, dailyViews)
                        .data(UNIQUE_VIEWS, statsRepository.buildUniqueViews(selector, parameter, startDate, endDate))
                        .data(DOMAIN_VIEWS, statsRepository.buildDomainStats(selector, parameter, startDate, endDate))
                        .data(PAGE_VIEWS, statsRepository.buildPageViews(selector, parameter, startDate, endDate))
                        .data(REFERER_VIEWS, statsRepository.buildReferrerStats(selector, parameter, startDate, endDate))
                        .data(PAGE_VIEWS_LAST_WEEK, statsRepository.buildPageViewsFromDate(selector,
                                                                                           parameter,
                                                                                           LocalDateTime.now()
                                                                                                        .minusDays(7)))
                        .data(TOTAL_VIEWS, dailyViews.stream()
                                                     .mapToLong(DailyStats::views)
                                                     .sum());
    }
}
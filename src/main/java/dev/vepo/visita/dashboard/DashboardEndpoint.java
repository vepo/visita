package dev.vepo.visita.dashboard;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private static final String REFERER_VIEWS = "refererViews";
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
        return load(Selector.NONE, null, startDate, endDate);
    }

    @GET
    @Operation(hidden = true)
    @Path("/domain/{domain}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance renderDomain(@PathParam("domain") String domain,
                                         @QueryParam("startDate") LocalDate startDate,
                                         @QueryParam("endDate") LocalDate endDate) {
        return load(Selector.DOMAIN, domain, startDate, endDate);
    }

    @GET
    @Operation(hidden = true)
    @Path("/referer/{referer}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance renderReferer(@PathParam("referer") String referer,
                                          @QueryParam("startDate") LocalDate startDate,
                                          @QueryParam("endDate") LocalDate endDate) {
        return load(Selector.REFERRER, referer, startDate, endDate);
    }

    private TemplateInstance load(Selector selector, String parameter, LocalDate startDate, LocalDate endDate) {
        logger.info("Loading dashboard for: {}={} startDate={}", selector, parameter, startDate);
        var dailyViews = statsRepository.buildDailyViews(selector, parameter);
        return dashboard.data(DAILY_VIEWS, dailyViews)
                        .data(UNIQUE_VIEWS, statsRepository.findUniqueUsersByPeriod(selector, parameter))
                        .data(DOMAIN_VIEWS, statsRepository.findAllDomainStats(selector, parameter))
                        .data(PAGE_VIEWS, statsRepository.findAllPageViews(selector, parameter))
                        .data(REFERER_VIEWS, statsRepository.findAllRefererStats(selector, parameter))
                        .data(PAGE_VIEWS_LAST_WEEK, statsRepository.findPageViewsFromDate(selector, parameter,
                                                                                          LocalDateTime.now()
                                                                                                       .minusDays(7)))
                        .data(TOTAL_VIEWS, dailyViews.stream()
                                                     .mapToLong(DailyStats::views)
                                                     .sum());
    }
}
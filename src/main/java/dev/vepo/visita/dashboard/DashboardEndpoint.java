package dev.vepo.visita.dashboard;

import java.time.LocalDateTime;

import org.eclipse.microprofile.openapi.annotations.Operation;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/dashboard")
public class DashboardEndpoint {

    private static final String TOTAL_VIEWS = "totalViews";
    private static final String PAGE_VIEWS_LAST_WEEK = "pageViewsLastWeek";
    private static final String PAGE_VIEWS = "pageViews";
    private static final String REFERER_VIEWS = "refererViews";
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
    public TemplateInstance render() {
        var dailyViews = statsRepository.buildDailyViews(Selector.NONE, null);
        return dashboard.data(DAILY_VIEWS, dailyViews)
                        .data(UNIQUE_VIEWS, statsRepository.findUniqueUsersByPeriod(Selector.NONE, null))
                        .data(PAGE_VIEWS, statsRepository.findAllPageViews(Selector.NONE, null))
                        .data(REFERER_VIEWS, statsRepository.findAllRefererStats(Selector.NONE, null))
                        .data(PAGE_VIEWS_LAST_WEEK, statsRepository.findPageViewsFromDate(Selector.NONE,
                                                                                          null,
                                                                                          LocalDateTime.now()
                                                                                                       .minusDays(7)))
                        .data(TOTAL_VIEWS, dailyViews.stream()
                                                     .mapToLong(DailyStats::views)
                                                     .sum());
    }

    @GET
    @Operation(hidden = true)
    @Path("/domain/{domain}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance renderDomain(@PathParam("domain") String domain) {
        var dailyViews = statsRepository.buildDailyViews(Selector.DOMAIN, domain);
        return dashboard.data(DAILY_VIEWS, dailyViews)
                        .data(UNIQUE_VIEWS, statsRepository.findUniqueUsersByPeriod(Selector.DOMAIN, domain))
                        .data(PAGE_VIEWS, statsRepository.findAllPageViews(Selector.DOMAIN, domain))
                        .data(REFERER_VIEWS, statsRepository.findAllRefererStats(Selector.DOMAIN, domain))
                        .data(PAGE_VIEWS_LAST_WEEK, statsRepository.findPageViewsFromDate(Selector.DOMAIN,
                                                                                          domain,
                                                                                          LocalDateTime.now()
                                                                                                       .minusDays(7)))
                        .data(TOTAL_VIEWS, dailyViews.stream()
                                                     .mapToLong(DailyStats::views)
                                                     .sum());
    }

    @GET
    @Operation(hidden = true)
    @Path("/referer/{referer}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance renderReferer(@PathParam("referer") String referer) {
        var dailyViews = statsRepository.buildDailyViews(Selector.REFERRER, referer);
        return dashboard.data(DAILY_VIEWS, dailyViews)
                        .data(UNIQUE_VIEWS, statsRepository.findUniqueUsersByPeriod(Selector.REFERRER, referer))
                        .data(PAGE_VIEWS, statsRepository.findAllPageViews(Selector.REFERRER, referer))
                        .data(REFERER_VIEWS, statsRepository.findAllRefererStats(Selector.REFERRER, referer))
                        .data(PAGE_VIEWS_LAST_WEEK, statsRepository.findPageViewsFromDate(Selector.REFERRER,
                                                                                          referer,
                                                                                          LocalDateTime.now()
                                                                                                       .minusDays(7)))
                        .data(TOTAL_VIEWS, dailyViews.stream()
                                                     .mapToLong(DailyStats::views)
                                                     .sum());
    }
}
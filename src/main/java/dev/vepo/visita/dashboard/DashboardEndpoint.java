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
    public TemplateInstance render() {
        return load(Selector.NONE, null);
    }

    @GET
    @Operation(hidden = true)
    @Path("/domain/{domain}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance renderDomain(@PathParam("domain") String domain) {
        return load(Selector.DOMAIN, domain);
    }

    @GET
    @Operation(hidden = true)
    @Path("/referer/{referer}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance renderReferer(@PathParam("referer") String referer) {
        return load(Selector.REFERRER, referer);
    }

    private TemplateInstance load(Selector selector, String parameter) {
        var dailyViews = statsRepository.buildDailyViews(selector, parameter);
        return dashboard.data(DAILY_VIEWS, dailyViews)
                        .data(UNIQUE_VIEWS, statsRepository.findUniqueUsersByPeriod(selector, parameter))
                        .data(DOMAIN_VIEWS, statsRepository.findAllDomainStats(selector, parameter))
                        .data(PAGE_VIEWS, statsRepository.findAllPageViews(selector, parameter))
                        .data(REFERER_VIEWS, statsRepository.findAllRefererStats(selector, parameter))
                        .data(PAGE_VIEWS_LAST_WEEK, statsRepository.findPageViewsFromDate(selector,
                                                                                          parameter,
                                                                                          LocalDateTime.now()
                                                                                                       .minusDays(7)))
                        .data(TOTAL_VIEWS, dailyViews.stream()
                                                     .mapToLong(DailyStats::views)
                                                     .sum());
    }
}
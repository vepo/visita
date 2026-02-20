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
        return dashboard.data("dailyViews", dailyViews)
                        .data("pageViews", statsRepository.findAllPageViews(Selector.NONE, null))
                        .data("referrerViews", statsRepository.findAllReferrerStats(Selector.NONE, null))
                        .data("pageViewsLastWeek", statsRepository.findPageViewsFromDate(Selector.NONE,
                                                                                         null,
                                                                                         LocalDateTime.now()
                                                                                                      .minusDays(7)))
                        .data("totalViews", dailyViews.stream()
                                                      .mapToLong(DailyStats::views)
                                                      .sum());
    }

    @GET
    @Operation(hidden = true)
    @Path("/domain/{domain}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance renderDomain(@PathParam("domain") String domain) {
        var dailyViews = statsRepository.buildDailyViews(Selector.DOMAIN, domain);
        return dashboard.data("dailyViews", dailyViews)
                        .data("pageViews", statsRepository.findAllPageViews(Selector.DOMAIN, domain))
                        .data("referrerViews", statsRepository.findAllReferrerStats(Selector.DOMAIN, domain))
                        .data("pageViewsLastWeek", statsRepository.findPageViewsFromDate(Selector.DOMAIN,
                                                                                         domain,
                                                                                         LocalDateTime.now()
                                                                                                      .minusDays(7)))
                        .data("totalViews", dailyViews.stream()
                                                      .mapToLong(DailyStats::views)
                                                      .sum());
    }

    @GET
    @Operation(hidden = true)
    @Path("/referrer/{referrer}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance renderReferrer(@PathParam("referrer") String referrer) {
        var dailyViews = statsRepository.buildDailyViews(Selector.REFERRER, referrer);
        return dashboard.data("dailyViews", dailyViews)
                        .data("pageViews", statsRepository.findAllPageViews(Selector.REFERRER, referrer))
                        .data("referrerViews", statsRepository.findAllReferrerStats(Selector.REFERRER, referrer))
                        .data("pageViewsLastWeek", statsRepository.findPageViewsFromDate(Selector.REFERRER,
                                                                                         referrer,
                                                                                         LocalDateTime.now()
                                                                                                      .minusDays(7)))
                        .data("totalViews", dailyViews.stream()
                                                      .mapToLong(DailyStats::views)
                                                      .sum());
    }
}
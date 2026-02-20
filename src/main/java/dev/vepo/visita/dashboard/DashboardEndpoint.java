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

    private final DashboardService dashboardService;

    @Inject
    public DashboardEndpoint(DashboardService dashboardService, Template dashboard) {
        this.dashboardService = dashboardService;
        this.dashboard = dashboard;
    }

    @GET
    @Operation(hidden = true)
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance renderDashboard() {
        var dailyViews = dashboardService.getDailyViews();
        return dashboard.data("dailyViews", dailyViews)
                        .data("pageViews", dashboardService.getPageViews())
                        .data("referrerViews", dashboardService.getReferrerStats())
                        .data("pageViewsLastWeek", dashboardService.getPageViews(LocalDateTime.now()
                                                                                              .minusDays(7)))
                        .data("totalViews", dailyViews.stream()
                                                      .mapToLong(DailyStats::views)
                                                      .sum());
    }

    @GET
    @Operation(hidden = true)
    @Path("/{hostname}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance renderDashboard(@PathParam("hostname") String hostname) {
        var dailyViews = dashboardService.getDailyViews(hostname);
        return dashboard.data("dailyViews", dailyViews)
                        .data("pageViews", dashboardService.getPageViews(hostname))
                        .data("referrerViews", dashboardService.getReferrerStats(hostname))
                        .data("pageViewsLastWeek", dashboardService.getPageViews(hostname,
                                                                                 LocalDateTime.now()
                                                                                              .minusDays(7)))
                        .data("totalViews", dailyViews.stream()
                                                      .mapToLong(DailyStats::views)
                                                      .sum());
    }
}
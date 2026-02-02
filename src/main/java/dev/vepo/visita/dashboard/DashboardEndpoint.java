package dev.vepo.visita.dashboard;

import java.time.LocalDateTime;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
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
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getDashboard() {
        var dailyViews = dashboardService.getDailyViews();
        return dashboard.data("dailyViews", dailyViews)
                        .data("pageViews", dashboardService.getPageViews())
                        .data("pageViewsLastWeek", dashboardService.getPageViews(LocalDateTime.now()
                                                                                              .minusDays(7)))
                        .data("totalViews", dailyViews.stream()
                                                      .mapToLong(DailyStats::views)
                                                      .sum());
    }
}
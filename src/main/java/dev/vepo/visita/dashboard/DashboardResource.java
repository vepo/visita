package dev.vepo.visita.dashboard;

import java.time.LocalDateTime;

import dev.vepo.visita.ViewsService;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/dashboard")
public class DashboardResource {

    private final Template dashboard;

    private final ViewsService visitaService;

    @Inject
    public DashboardResource(ViewsService visitaService, Template dashboard) {
        this.visitaService = visitaService;
        this.dashboard = dashboard;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getDashboard() {
        var dailyViews = visitaService.getDailyViews();
        return dashboard.data("dailyViews", dailyViews)
                        .data("pageViews", visitaService.getPageViews())
                        .data("pageViewsLastWeek", visitaService.getPageViews(LocalDateTime.now()
                                                                                           .minusDays(7)))
                        .data("totalViews", dailyViews.stream()
                                                      .mapToLong(DailyStats::views)
                                                      .sum());
    }
}
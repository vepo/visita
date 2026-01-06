package dev.vepo.visita;

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

    private final VisitaService visitaService;

    @Inject
    public DashboardResource(VisitaService visitaService, Template dashboard) {
        this.visitaService = visitaService;
        this.dashboard = dashboard;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getDashboard() {
        var visitasDiarias = visitaService.getVisitasDiarias();
        var visitasPorPagina = visitaService.getVisitasPorPagina();

        long totalVisitas = visitasDiarias.stream()
                                          .mapToLong(VisitaDiaria::visitas)
                                          .sum();

        return dashboard.data("visitasDiarias", visitasDiarias)
                        .data("visitasPorPagina", visitasPorPagina)
                        .data("totalVisitas", totalVisitas);
    }
}
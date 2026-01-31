package dev.vepo.visita.dashboard;

import java.time.LocalDateTime;

import dev.vepo.visita.EstatisticaPorDia;
import dev.vepo.visita.VisitaService;
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
        var visitasPorPaginaUltimaSemana = visitaService.getVisitasPorPagina(LocalDateTime.now()
                                                                                          .minusDays(7));

        long totalVisitas = visitasDiarias.stream()
                                          .mapToLong(EstatisticaPorDia::visitas)
                                          .sum();

        return dashboard.data("visitasDiarias", visitasDiarias)
                        .data("visitasPorPagina", visitasPorPagina)
                        .data("visitasPorPaginaUltimaSemana", visitasPorPaginaUltimaSemana)
                        .data("totalVisitas", totalVisitas);
    }
}
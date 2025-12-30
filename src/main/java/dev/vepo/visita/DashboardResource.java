package dev.vepo.visita;

import java.util.List;
import java.util.Map;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

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
        List<Visita.VisitaDiaria> visitasDiarias = visitaService.getVisitasDiarias();
        List<Map<String, Object>> visitasPorPagina = visitaService.getVisitasPorPagina();
        
        long totalVisitas = visitasDiarias.stream()
            .mapToLong(Visita.VisitaDiaria::visitas)
            .sum();
        
        return dashboard
            .data("visitasDiarias", visitasDiarias)
            .data("visitasPorPagina", visitasPorPagina)
            .data("totalVisitas", totalVisitas);
    }
}
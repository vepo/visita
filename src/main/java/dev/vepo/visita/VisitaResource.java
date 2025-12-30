package dev.vepo.visita;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/visita")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class VisitaResource {
    private static final Logger logger = LoggerFactory.getLogger(VisitaResource.class);

    @Inject
    VisitaService visitaService;

    public static record IniciarVisitaRequest(String language, String pagina, String referer, String screenResolution,
            String tabId, long timestamp, String timezone, String userAgent, String userId) {
    }

    public static record FinalizarVisitaRequest(long id) {
    }

    public static record IniciarVisitaResponse(long id) {
    }

    @POST
    @Path("/acesso")
    public IniciarVisitaResponse registrarAcesso(IniciarVisitaRequest request) {
        logger.info("Registrando acesso! request={}", request);
        var visita = visitaService.registrarAcesso(request.pagina(), request.referer(), request.userAgent(),
                request.timezone());
        return new IniciarVisitaResponse(visita.id);
    }

    @POST
    @Path("/saida")
    public Response registrarSaida(FinalizarVisitaRequest request) {
        logger.info("Registrando saída! request={}", request);
        visitaService.registrarSaida(request.id());
        return Response.ok().build();
    }

    @GET
    @Path("/ping")
    public Response ping() {
        return Response.ok("OK").build();
    }
}
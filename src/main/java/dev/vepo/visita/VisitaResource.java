package dev.vepo.visita;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.NotFoundException;
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

    private final VisitaService visitaService;

    @Inject
    public VisitaResource(VisitaService visitaService) {
        this.visitaService = visitaService;
    }

    public static record IniciarVisitaRequest(String language, String page, String referer, String screenResolution,
                                              String tabId, long timestamp, String timezone, String userAgent, String userId) {}

    public static record FinalizarVisitaRequest(long id) {}

    public static record PingVisitaRequest(long id) {}

    public static record IniciarVisitaResponse(long id) {}

    public static record ViewVisitaResponse(long id, String page, String tabId, long timestamp, String userId) {}

    public static record ViewResponse(long id) {}

    @POST
    @Path("/access")
    public IniciarVisitaResponse access(IniciarVisitaRequest request) {
        logger.info("Registrando acesso! request={}", request);
        var visita = visitaService.registrarAcesso(request.page(), request.referer(), request.userAgent(),
                                                   request.timezone());
        return new IniciarVisitaResponse(visita.getId());
    }

    @POST
    @Path("/exit")
    public Response exit(FinalizarVisitaRequest request) {
        logger.info("Registrando saída! request={}", request);
        visitaService.registrarSaida(request.id());
        return Response.ok().build();
    }

    @POST
    @Path("/view")
    public ViewResponse view(ViewVisitaResponse request) {
        logger.info("Registrando view! request={}", request);
        var view = visitaService.registerView(request.id(), request.page());
        if (Objects.nonNull(view)) {
            return new ViewResponse(view.getId());
        } else {
            throw new NotFoundException("View not found!!! id=%s".formatted(request.id()));
        }
    }

    @POST
    @Path("/ping")
    public Response ping(PingVisitaRequest request) {
        logger.info("Registrando saída! request={}", request);
        visitaService.registraPing(request.id());
        return Response.ok().build();
    }
}
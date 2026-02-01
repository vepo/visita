package dev.vepo.visita;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
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

    private final ViewsService visitaService;

    @Inject
    public VisitaResource(ViewsService visitaService) {
        this.visitaService = visitaService;
    }

    @POST
    @Path("/access")
    public IniciarVisitaResponse access(@Valid IniciarVisitaRequest request) {
        logger.info("Registrando acesso! request={}", request);
        var visita = visitaService.registrarAcesso(request.page(), request.referrer(), request.userAgent(),
                                                   request.timezone(), request.timestamp());
        return new IniciarVisitaResponse(visita.getId());
    }

    @POST
    @Path("/exit")
    public Response exit(@Valid FinalizarVisitaRequest request) {
        logger.info("Registrando saída! request={}", request);
        visitaService.registrarSaida(request.id(), request.timestamp());
        return Response.ok().build();
    }

    @POST
    @Path("/view")
    public ViewResponse view(@Valid ViewVisitaResponse request) {
        logger.info("Registrando view! request={}", request);
        var view = visitaService.registerView(request.id(), request.page(), request.timestamp());
        if (Objects.nonNull(view)) {
            return new ViewResponse(view.getId());
        } else {
            throw new NotFoundException("View not found!!! id=%s".formatted(request.id()));
        }
    }

    @POST
    @Path("/ping")
    public Response ping(@Valid PingVisitaRequest request) {
        logger.info("Registrando ping! request={}", request);
        visitaService.registraPing(request.id(), request.timestamp());
        return Response.ok().build();
    }
}
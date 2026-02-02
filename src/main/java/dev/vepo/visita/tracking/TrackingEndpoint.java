package dev.vepo.visita.tracking;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.vepo.visita.ViewResponse;
import dev.vepo.visita.ViewsService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/tracking")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TrackingEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(TrackingEndpoint.class);

    private final ViewsService visitaService;

    @Inject
    public TrackingEndpoint(ViewsService visitaService) {
        this.visitaService = visitaService;
    }

    @POST
    @Path("/access")
    public TrackingStartResponse access(@Valid TrackingStartRequest request) {
        logger.info("Registering access! request={}", request);
        var view = visitaService.registrarAcesso(request.page(), request.referrer(), request.userAgent(),
                                                 request.timezone(), request.timestamp());
        logger.info("View created! view={}", view);
        return new TrackingStartResponse(view.getId());
    }

    @POST
    @Path("/exit")
    public Response exit(@Valid TrackingEndRequest request) {
        logger.info("Registering exit! request={}", request);
        visitaService.registrarSaida(request.id(), request.timestamp());
        return Response.ok().build();
    }

    @POST
    @Path("/view")
    public ViewResponse view(@Valid TrackingUpdateRequest request) {
        logger.info("Registering view! request={}", request);
        var view = visitaService.registerView(request.id(), request.page(), request.timestamp());
        if (Objects.nonNull(view)) {
            logger.info("View registered! view={}", view);
            return new ViewResponse(view.getId());
        } else {
            throw new NotFoundException("View not found!!! id=%s".formatted(request.id()));
        }
    }

    @POST
    @Path("/ping")
    public Response ping(@Valid TrackingPingRequest request) {
        logger.info("Registering ping! request={}", request);
        visitaService.registraPing(request.id(), request.timestamp());
        return Response.ok().build();
    }
}
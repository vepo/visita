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
        logger.info("Starting new tracking session - request={}", request);

        var view = visitaService.registrarAcesso(request.page(), request.referrer(), request.userAgent(),
                                                 request.timezone(), request.timestamp());

        logger.info("Tracking session created successfully - view={}", view);
        return new TrackingStartResponse(view.getId());
    }

    @POST
    @Path("/exit")
    public Response exit(@Valid TrackingEndRequest request) {
        logger.info("Registering session exit - request={}", request);

        visitaService.registrarSaida(request.id(), request.timestamp());

        logger.info("Session exit registered successfully - request={}", request);
        return Response.ok().build();
    }

    @POST
    @Path("/view")
    public ViewResponse view(@Valid TrackingUpdateRequest request) {
        logger.info("Updating view registration - sessionId={}, request={}", request);

        var view = visitaService.registerView(request.id(), request.page(), request.timestamp());

        if (Objects.nonNull(view)) {
            logger.info("View registration updated successfully - view={}", view);
            return new ViewResponse(view.getId());
        } else {
            logger.warn("Failed to update view - session not found: request={}", request);
            throw new NotFoundException("View not found with id=%s".formatted(request.id()));
        }
    }

    @POST
    @Path("/ping")
    public Response ping(@Valid TrackingPingRequest request) {
        logger.debug("Processing keep-alive ping - request={}", request);

        visitaService.registraPing(request.id(), request.timestamp());

        logger.debug("Keep-alive ping processed successfully - request={}", request);
        return Response.ok().build();
    }
}
package dev.vepo.visita.tracking;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.vepo.visita.ViewsService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/tracking")
@PermitAll
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TrackingEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(TrackingEndpoint.class);

    private final ViewsService visitaService;
    private final TrackingDomainValidator trackingDomainValidator;

    @Inject
    public TrackingEndpoint(ViewsService visitaService, TrackingDomainValidator trackingDomainValidator) {
        this.visitaService = visitaService;
        this.trackingDomainValidator = trackingDomainValidator;
    }

    @POST
    @TokenRequired
    @Path("/access")
    public Response access(@Valid TrackingStartRequest request) {
        logger.info("Starting new tracking session - request={}", request);

        try {
            var view = visitaService.registrarAcesso(request.page(), request.referrer(), request.userAgent(),
                                                     request.timezone(), request.userId(), request.tabId(),
                                                     request.timestamp());
            logger.info("Tracking session created successfully - view={}", view);
            return Response.ok(new TrackingResponse(view.getId())).build();
        } catch (IgnoredPathException exception) {
            logger.debug("Tracking skipped for ignored path - page={}", request.page());
            return Response.noContent().build();
        }
    }

    @POST
    @Path("/exit")
    public Response exit(@Valid TrackingEndRequest request, @Context ContainerRequestContext requestContext) {
        var hostname = firstNonBlank(requestContext.getHeaderString(TrackingTokenFilter.HOSTNAME_HEADER), request.domainHostname());
        var token = firstNonBlank(requestContext.getHeaderString(TrackingTokenFilter.TOKEN_HEADER), request.domainToken());
        trackingDomainValidator.requireValidDomain(hostname, token);

        logger.info("Registering session exit - request={}", request);

        visitaService.registrarSaida(request.id(), request.timestamp());

        logger.info("Session exit registered successfully - request={}", request);
        return Response.ok().build();
    }

    @POST
    @TokenRequired
    @Path("/view")
    public Response view(@Valid TrackingUpdateRequest request) {
        logger.info("Updating view registration - sessionId={}, request={}", request);

        try {
            var view = visitaService.registerView(request.id(), request.page(), request.timestamp());

            if (Objects.nonNull(view)) {
                logger.info("View registration updated successfully - view={}", view);
                return Response.ok(new TrackingResponse(view.getId())).build();
            } else {
                logger.warn("Failed to update view - session not found: request={}", request);
                throw new NotFoundException("View not found with id=%s".formatted(request.id()));
            }
        } catch (IgnoredPathException exception) {
            logger.debug("View update skipped for ignored path - page={}", request.page());
            return Response.noContent().build();
        }
    }

    @POST
    @TokenRequired
    @Path("/ping")
    public Response ping(@Valid TrackingPingRequest request) {
        logger.debug("Processing keep-alive ping - request={}", request);

        visitaService.registraPing(request.id(), request.timestamp());

        logger.debug("Keep-alive ping processed successfully - request={}", request);
        return Response.ok().build();
    }

    private String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback;
    }
}

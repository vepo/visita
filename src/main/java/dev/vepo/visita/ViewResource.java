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
public class ViewResource {
    private static final Logger logger = LoggerFactory.getLogger(ViewResource.class);

    private final ViewsService visitaService;

    @Inject
    public ViewResource(ViewsService visitaService) {
        this.visitaService = visitaService;
    }

    @POST
    @Path("/access")
    public StartViewResponse access(@Valid StartViewRequest request) {
        logger.info("Registering access! request={}", request);
        var view = visitaService.registrarAcesso(request.page(), request.referrer(), request.userAgent(),
                                                 request.timezone(), request.timestamp());
        logger.info("View created! view={}", view);
        return new StartViewResponse(view.getId());
    }

    @POST
    @Path("/exit")
    public Response exit(@Valid CloseViewRequest request) {
        logger.info("Registering exit! request={}", request);
        visitaService.registrarSaida(request.id(), request.timestamp());
        return Response.ok().build();
    }

    @POST
    @Path("/view")
    public ViewResponse view(@Valid UpdateViewRequest request) {
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
    public Response ping(@Valid PingViewRequest request) {
        logger.info("Registering ping! request={}", request);
        visitaService.registraPing(request.id(), request.timestamp());
        return Response.ok().build();
    }
}
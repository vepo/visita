package dev.vepo.visita.tracking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.vepo.visita.domain.DomainRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

@ApplicationScoped
@Path("/api/tracking/config")
@TokenRequired
@Produces(MediaType.APPLICATION_JSON)
public class TrackingConfigEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(TrackingConfigEndpoint.class);

    private final DomainRepository domainRepository;

    @Inject
    public TrackingConfigEndpoint(DomainRepository domainRepository) {
        this.domainRepository = domainRepository;
    }

    @GET
    public TrackingConfigResponse config(@Context ContainerRequestContext requestContext) {
        var hostname = requestContext.getHeaderString(TrackingTokenFilter.HOSTNAME_HEADER);
        var token = requestContext.getHeaderString(TrackingTokenFilter.TOKEN_HEADER);
        logger.debug("Loading tracking config for domain={}", hostname);
        var domain = domainRepository.findByHostnameAndToken(hostname, token)
                                     .orElseThrow(() -> new NotAuthorizedException("Invalid domain token"));
        return new TrackingConfigResponse(domain.parsedIgnoredPathPatterns());
    }
}

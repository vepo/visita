package dev.vepo.visita.tracking;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.Provider;

@Provider
@TokenRequired
public class TrackingTokenFilter implements ContainerRequestFilter {

    public static final String TOKEN_HEADER = "VISITA-DOMAIN-TOKEN";
    public static final String HOSTNAME_HEADER = "VISITA-DOMAIN-HOSTNAME";
    private static final Logger logger = LoggerFactory.getLogger(TrackingTokenFilter.class);
    private final TrackingDomainValidator trackingDomainValidator;

    @Inject
    public TrackingTokenFilter(TrackingDomainValidator trackingDomainValidator) {
        this.trackingDomainValidator = trackingDomainValidator;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        logger.debug("Validating tracking domain token");

        var token = requestContext.getHeaderString(TOKEN_HEADER);
        var hostname = requestContext.getHeaderString(HOSTNAME_HEADER);

        try {
            trackingDomainValidator.requireValidDomain(hostname, token);
            logger.debug("Valid tracking domain hostname={}", hostname);
        } catch (BadRequestException exception) {
            requestContext.abortWith(Response.status(Status.BAD_REQUEST).build());
        } catch (NotAuthorizedException exception) {
            logger.warn("Invalid tracking domain hostname={}", hostname);
            requestContext.abortWith(Response.status(Status.UNAUTHORIZED).build());
        }
    }

}

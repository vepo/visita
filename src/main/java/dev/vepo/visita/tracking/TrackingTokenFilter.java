package dev.vepo.visita.tracking;

import java.io.IOException;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.vepo.visita.domain.DomainRepository;
import jakarta.inject.Inject;
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
    private final DomainRepository domainRepository;

    @Inject
    public TrackingTokenFilter(DomainRepository domainRepository) {
        this.domainRepository = domainRepository;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        logger.info("Validating tracking code! requestContext={}", requestContext);

        
        var token = requestContext.getHeaderString(TOKEN_HEADER);
        if (Objects.isNull(token) || token.isBlank()) {
            requestContext.abortWith(Response.status(Status.BAD_REQUEST).build());
            return;
        }

        var hostname = requestContext.getHeaderString(HOSTNAME_HEADER);
        if (Objects.isNull(hostname) || hostname.isBlank()) {
            requestContext.abortWith(Response.status(Status.BAD_REQUEST).build());
            return;
        }

        logger.info("Validating domain={} token={}", hostname, token);
        this.domainRepository.findByHostnameAndToken(hostname, token)
                             .ifPresentOrElse(domain -> logger.info("Valid domain! domain={}", domain),
                                              () -> {
                                                  logger.warn("Invalid domain! request={}", requestContext.getRequest());
                                                  requestContext.abortWith(Response.status(Status.UNAUTHORIZED).build());
                                              });
    }

}

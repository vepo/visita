package dev.vepo.visita.domain.disable;

import dev.vepo.visita.domain.DomainRepository;
import dev.vepo.visita.domain.DomainResponse;
import dev.vepo.visita.shared.security.RequiredRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/domains/{domainId}/disable")
@ApplicationScoped
@RolesAllowed(RequiredRoles.ADMIN)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DisableDomainEndpoint {
    private final DomainRepository domainRepository;

    @Inject
    public DisableDomainEndpoint(DomainRepository domainRepository) {
        this.domainRepository = domainRepository;
    }

    @PATCH
    @Transactional
    public DomainResponse disable(@PathParam("domainId") long domainId) {
        return DomainResponse.from(this.domainRepository.findById(domainId)
                                                        .map(domain -> {
                                                            domain.setDisabled(true);
                                                            return this.domainRepository.save(domain);
                                                        })
                                                        .orElseThrow(() -> new NotFoundException("Domain not found!!! domainId=%d".formatted(domainId))));
    }
}

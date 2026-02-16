package dev.vepo.visita.domain.find;

import dev.vepo.visita.domain.DomainRepository;
import dev.vepo.visita.domain.DomainResponse;
import dev.vepo.visita.shared.security.RequiredRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/domains/{domainId}")
@ApplicationScoped
@RolesAllowed(RequiredRoles.ADMIN)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FindDomainByIdEndpoint {
    private final DomainRepository domainRepository;

    @Inject
    public FindDomainByIdEndpoint(DomainRepository domainRepository) {
        this.domainRepository = domainRepository;
    }

    @GET
    public DomainResponse findById(@PathParam("domainId") long domainId) {
        return DomainResponse.from(this.domainRepository.findById(domainId)
                                                        .orElseThrow(() -> new NotFoundException("Domain not found! domainId=%d".formatted(domainId))));
    }
}

package dev.vepo.visita.domain.update;

import dev.vepo.visita.domain.DomainRepository;
import dev.vepo.visita.domain.DomainResponse;
import dev.vepo.visita.shared.security.RequiredRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/domains/{domainId}")
@ApplicationScoped
@RolesAllowed(RequiredRoles.ADMIN)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UpdateDomainEndpoint {
    private final DomainRepository domainRepository;

    @Inject
    public UpdateDomainEndpoint(DomainRepository domainRepository) {
        this.domainRepository = domainRepository;
    }

    @PUT
    @Transactional
    public DomainResponse update(@PathParam("domainId") long domainId, @Valid UpdateDomainRequest request) {
        var domain = this.domainRepository.findById(domainId)
                                          .orElseThrow(() -> new NotFoundException("Domain not found! domainId=%d".formatted(domainId)));
        domain.setHostname(request.hostname());
        return DomainResponse.from(this.domainRepository.save(domain));
    }
}

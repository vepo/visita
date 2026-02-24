package dev.vepo.visita.domain.create;

import java.util.UUID;

import dev.vepo.visita.domain.Domain;
import dev.vepo.visita.domain.DomainRepository;
import dev.vepo.visita.domain.DomainResponse;
import dev.vepo.visita.shared.security.RequiredRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

@Path("/api/domains")
@ApplicationScoped
@RolesAllowed(RequiredRoles.ADMIN)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CreateDomainEndpoint {
    private final DomainRepository domainRepository;

    @Inject
    public CreateDomainEndpoint(DomainRepository domainRepository) {
        this.domainRepository = domainRepository;
    }

    @POST
    @Transactional
    public DomainResponse create(@Valid CreateDomainRequest request) {
        var prevDomain = this.domainRepository.findByHostname(request.hostname());
        if (prevDomain.isPresent()) {
            throw new WebApplicationException("Domain already exists!!! domain=%s".formatted(prevDomain.get()),
                                              Status.CONFLICT);
        }

        return DomainResponse.from(this.domainRepository.save(new Domain(request.hostname(),
                                                                         UUID.randomUUID().toString())));
    }
}

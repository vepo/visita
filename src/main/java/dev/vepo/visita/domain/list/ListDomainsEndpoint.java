package dev.vepo.visita.domain.list;

import java.util.List;

import dev.vepo.visita.domain.DomainRepository;
import dev.vepo.visita.domain.DomainResponse;
import dev.vepo.visita.shared.security.RequiredRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/domains")
@ApplicationScoped
@RolesAllowed(RequiredRoles.ADMIN)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ListDomainsEndpoint {
    private final DomainRepository domainRepository;

    @Inject
    public ListDomainsEndpoint(DomainRepository domainRepository) {
        this.domainRepository = domainRepository;
    }

    @GET
    public List<DomainResponse> list() {
        return this.domainRepository.findAll()
                                    .stream()
                                    .map(DomainResponse::from)
                                    .toList();
    }
}

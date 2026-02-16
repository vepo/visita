package dev.vepo.visita.domain.token.regenerate;

import java.util.UUID;

import dev.vepo.visita.domain.DomainRepository;
import dev.vepo.visita.domain.DomainResponse;
import dev.vepo.visita.shared.security.RequiredRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/domains/{domainId}/regenerate-token")
@ApplicationScoped
@RolesAllowed(RequiredRoles.ADMIN)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RegenerateTokenEndpoint {
        private final DomainRepository domainRepository;

    @Inject
    public RegenerateTokenEndpoint(DomainRepository domainRepository) {
        this.domainRepository = domainRepository;
    }

    @POST
    @Transactional
    public DomainResponse regenerate(@PathParam("domainId") long domainId) {
        return DomainResponse.from(this.domainRepository.findById(domainId)
                                                        .map(domain -> {
                                                            domain.setToken(UUID.randomUUID().toString());
                                                            return this.domainRepository.save(domain);
                                                        })
                                                        .orElseThrow(() -> new NotFoundException("Domain not found!!! domainId=%d".formatted(domainId))));
    }
}

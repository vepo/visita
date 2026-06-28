package dev.vepo.visita.tracking;

import dev.vepo.visita.domain.DomainRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotAuthorizedException;

@ApplicationScoped
public class TrackingDomainValidator {

    private final DomainRepository domainRepository;

    public TrackingDomainValidator(DomainRepository domainRepository) {
        this.domainRepository = domainRepository;
    }

    public void requireValidDomain(String hostname, String token) {
        if (isBlank(hostname) || isBlank(token)) {
            throw new BadRequestException();
        }
        domainRepository.findByHostnameAndToken(hostname, token)
                        .orElseThrow(() -> new NotAuthorizedException("Invalid domain token"));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

}

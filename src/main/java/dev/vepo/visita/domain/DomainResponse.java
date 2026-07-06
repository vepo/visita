package dev.vepo.visita.domain;

import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record DomainResponse(long id, String hostname, String token, boolean disabled, List<String> ignoredPathPatterns) {
    public static DomainResponse from(Domain domain) {
        return new DomainResponse(domain.getId(),
                                  domain.getHostname(),
                                  domain.getToken(),
                                  domain.isDisabled(),
                                  domain.parsedIgnoredPathPatterns());
    }
}

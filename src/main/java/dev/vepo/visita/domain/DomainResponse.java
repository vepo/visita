package dev.vepo.visita.domain;

public record DomainResponse(long id, String hostname, String token, boolean disabled) {
    public static DomainResponse from(Domain domain) {
        return new DomainResponse(domain.getId(),
                                  domain.getHostname(),
                                  domain.getToken(),
                                  domain.isDisabled());
    }
}

package dev.vepo.infra;

import dev.vepo.visita.domain.Domain;
import dev.vepo.visita.domain.DomainRepository;

public class DomainBuilder {

    private String hostname;
    private String token;
    private java.util.List<String> ignoredPathPatterns;

    public DomainBuilder() {
        this.hostname = null;
        this.token = null;
    }

    public DomainBuilder withHostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    public DomainBuilder withToken(String token) {
        this.token = token;
        return this;
    }

    public DomainBuilder withIgnoredPathPatterns(java.util.List<String> ignoredPathPatterns) {
        this.ignoredPathPatterns = ignoredPathPatterns;
        return this;
    }

    private void updateDatabase() {
        var repo = Given.inject(DomainRepository.class);
        repo.save(new Domain(hostname, token, ignoredPathPatterns));
    }

    public void persist() {
        Given.withTransaction(this::updateDatabase);
    }

}

package dev.vepo.infra;

import dev.vepo.visita.Domain;
import dev.vepo.visita.DomainRepository;

public class DomainBuilder {

    private String hostname;

    public DomainBuilder() {
        hostname = null;
    }

    public DomainBuilder withHostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    private void updateDatabase() {
        var repo = Given.inject(DomainRepository.class);
        repo.save(new Domain(hostname));
    }

    public void persist() {
        Given.withTransaction(this::updateDatabase);
    }

}

package dev.vepo.infra;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import dev.vepo.visita.View;
import dev.vepo.visita.ViewRepository;
import dev.vepo.visita.domain.Domain;
import dev.vepo.visita.domain.DomainRepository;
import dev.vepo.visita.page.Page;
import dev.vepo.visita.page.PageRepository;

public class ViewBuilder {

    private String page;
    private Integer length;

    public ViewBuilder() {
        page = null;
        length = null;
    }

    public ViewBuilder withPage(String page) {
        this.page = page;
        return this;
    }

    public ViewBuilder withLength(int length) {
        this.length = length;
        return this;
    }

    private void updateDatabase() {
        var repo = Given.inject(ViewRepository.class);
        var domainRepository = Given.inject(DomainRepository.class);
        var pageRepository = Given.inject(PageRepository.class);
        var pageUrl = URI.create(page);
        var domain = domainRepository.findByHostname(pageUrl.getHost())
                                     .orElseGet(() -> domainRepository.save(new Domain(pageUrl.getHost(), "token")));
        var page = pageRepository.findByHostnameAndPath(pageUrl.getHost(), pageUrl.getPath())
                                 .orElseGet(() -> pageRepository.save(new Page(domain, pageUrl.getPath())));
        var visita = new View(page,
                              "test",
                              "test",
                              "test",
                              System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(length));
        visita.setEndTimestamp(LocalDateTime.now());
        repo.save(visita);
    }

    public void persist() {
        Given.withTransaction(this::updateDatabase);
    }

}

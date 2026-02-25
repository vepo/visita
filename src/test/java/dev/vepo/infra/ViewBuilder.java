package dev.vepo.infra;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
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
    private String referer;
    private Instant start;
    private String userId;
    private String tabId;

    public ViewBuilder() {
        page = null;
        length = null;
        start = Instant.now();
    }

    public ViewBuilder withPage(String page) {
        this.page = page;
        return this;
    }

    public ViewBuilder withReferer(String referer) {
        this.referer = referer;
        return this;
    }

    public ViewBuilder withLength(int length) {
        this.length = length;
        return this;
    }

    public ViewBuilder withStart(Instant start) {
        this.start = start;
        return this;
    }

    public ViewBuilder withAccessDate(LocalDate accessDate) {
        this.start = accessDate.atTime(8, 0).toInstant(ZoneOffset.UTC);
        return this;
    }

    public ViewBuilder withUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public ViewBuilder withTabId(String tabId) {
        this.tabId = tabId;
        return this;
    }

    private View updateDatabase() {
        var repo = Given.inject(ViewRepository.class);
        var domainRepository = Given.inject(DomainRepository.class);
        var pageRepository = Given.inject(PageRepository.class);
        var pageUrl = URI.create(page);
        var domain = domainRepository.findByHostname(pageUrl.getHost())
                                     .orElseGet(() -> domainRepository.save(new Domain(pageUrl.getHost(), "token")));
        var page = pageRepository.findByHostnameAndPath(pageUrl.getHost(), pageUrl.getPath())
                                 .orElseGet(() -> pageRepository.save(new Page(domain, pageUrl.getPath())));
        var visita = new View(page,
                              Optional.ofNullable(this.userId).orElseGet(() -> UUID.randomUUID().toString()),
                              Optional.ofNullable(this.tabId).orElseGet(() -> UUID.randomUUID().toString()),
                              referer,
                              "test",
                              "test",
                              start.toEpochMilli() - TimeUnit.SECONDS.toMillis(length));
        visita.setEndTimestamp(LocalDateTime.now());
        return repo.save(visita);
    }

    public View persist() {
        return Given.withTransaction(this::updateDatabase);
    }
}

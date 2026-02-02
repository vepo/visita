package dev.vepo.visita;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.vepo.visita.domain.DomainRepository;
import dev.vepo.visita.page.Page;
import dev.vepo.visita.page.PageRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class ViewsService {
    private static final Logger logger = LoggerFactory.getLogger(ViewsService.class);

    private final ViewRepository visitaRepository;
    private final PageRepository pageRepository;
    private final DomainRepository domainRepository;

    @Inject
    public ViewsService(ViewRepository visitaRepository,
                        PageRepository pageRepository,
                        DomainRepository domainRepository) {
        this.visitaRepository = visitaRepository;
        this.pageRepository = pageRepository;
        this.domainRepository = domainRepository;
    }

    @Transactional
    public View registrarAcesso(String page, String referer, String userAgent, String ip, long timestamp) {
        var pageUri = URI.create(page);
        var pageEntity = pageRepository.findByHostnameAndPath(pageUri.getHost(), pageUri.getPath())
                                       .orElseGet(() -> createNewPage(pageUri));
        return visitaRepository.save(new View(pageEntity, referer, userAgent, ip, timestamp));
    }

    private Page createNewPage(URI pageUri) {
        logger.debug("Creating new page for URI! uri={}", pageUri);
        var domain = domainRepository.findByHostname(pageUri.getHost())
                                     .orElseThrow(() -> new NotFoundException("Domain not found! domain=%s".formatted(pageUri.getHost())));
        logger.debug("Found domain! domain={}", domain);
        var page = new Page(domain, pageUri.getPath());
        logger.debug("Persisting new page! page={}", pageUri);
        return pageRepository.save(page);
    }

    @Transactional
    public void registrarSaida(Long id, long timestamp) {
        View visita = visitaRepository.findById(id);
        if (Objects.nonNull(visita)) {
            visita.setEndTimestamp(Instant.ofEpochMilli(timestamp)
                                          .atZone(ZoneId.systemDefault())
                                          .toLocalDateTime());
        } else {
            logger.warn("Visita not found! id={}", id);
        }
    }

    @Transactional
    public View registerView(long id, String page, long timestamp) {
        var visita = visitaRepository.findById(id);
        logger.info("View found! view={}", visita);
        if (Objects.nonNull(visita)) {
            var urlPath = URI.create(page).getPath();
            if (visita.isSamePage(urlPath)) {
                visita.extendDuration(timestamp);
                visitaRepository.save(visita);
                return visita;
            } else {
                // finish last view
                visita.setEndTimestamp(Instant.ofEpochMilli(timestamp)
                                              .atZone(ZoneId.systemDefault())
                                              .toLocalDateTime());
                visitaRepository.save(visita);
                var pageUri = URI.create(page);
                var pageEntity = pageRepository.findByHostnameAndPath(pageUri.getHost(), pageUri.getPath())
                                               .orElseGet(() -> createNewPage(pageUri));
                // start a new view
                return visitaRepository.save(new View(pageEntity, timestamp, visita));
            }
        } else {
            logger.warn("Visita not found! id={}", id);
            return null;
        }
    }

    @Transactional
    public void registraPing(Long visitaId, long timestamp) {
        View visita = visitaRepository.findById(visitaId);
        if (Objects.nonNull(visita)) {
            visita.extendDuration(timestamp);
            visitaRepository.save(visita);
        } else {
            logger.warn("Visita not found! id={}", visitaId);
        }
    }
}
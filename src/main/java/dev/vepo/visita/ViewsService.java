package dev.vepo.visita;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ViewsService {
    private static final Logger logger = LoggerFactory.getLogger(ViewsService.class);

    private final VisitaRepository visitaRepository;

    @Inject
    public ViewsService(VisitaRepository visitaRepository) {
        this.visitaRepository = visitaRepository;
    }

    @Transactional
    public View registrarAcesso(String page, String referer, String userAgent, String ip, long timestamp) {
        return visitaRepository.save(new View(page, referer, userAgent, ip, timestamp));
    }

    @Transactional
    public void registrarSaida(Long id, long timestamp) {
        View visita = visitaRepository.findById(id);
        if (Objects.nonNull(visita)) {
            visita.setDataSaida(Instant.ofEpochMilli(timestamp)
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
            if (visita.isSamePage(page)) {
                visita.extendDuration(timestamp);
                visitaRepository.save(visita);
                return visita;
            }  else {
                // finish last view
                visita.setDataSaida(Instant.ofEpochMilli(timestamp)
                                           .atZone(ZoneId.systemDefault())
                                           .toLocalDateTime());
                visitaRepository.save(visita);
                
                // start a new view
                return visitaRepository.save(new View(page, timestamp, visita));
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

    public List<DailyStats> getDailyViews() {
        return visitaRepository.findDailyViews();
    }

    public List<PageStats> getPageViews() {
        return getPageViews(LocalDateTime.MIN);
    }

    public List<PageStats> getPageViews(LocalDateTime startDate) {
        return visitaRepository.findPageViews(startDate);
    }
}
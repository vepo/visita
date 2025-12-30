package dev.vepo.visita;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class VisitaService {
    private static final Logger logger = LoggerFactory.getLogger(VisitaService.class);

    private final EntityManager entityManager;
    
    @Inject
    public VisitaService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public Visita registrarAcesso(String pagina, String referer, String userAgent, String ip) {
        Visita visita = new Visita();
        visita.pagina = pagina;
        visita.referer = referer;
        visita.userAgent = userAgent;
        visita.ip = ip;
        visita.dataAcesso = LocalDateTime.now();
        visita.persist();
        return visita;
    }

    @Transactional
    public void registrarSaida(Long visitaId) {
        Visita visita = Visita.findById(visitaId);
        if (visita != null) {
            visita.dataSaida = LocalDateTime.now();
            if (visita.dataAcesso != null && visita.dataSaida != null) {
                visita.duracao = ChronoUnit.SECONDS.between(visita.dataAcesso, visita.dataSaida);
            }
        } else {
            logger.warn("Visita not found! id={}", visitaId);
        }
    }

    public List<Visita.VisitaDiaria> getVisitasDiarias() {
        String query = """
                SELECT DATE(v.dataAcesso) as data,
                       COUNT(v.id) as visitas,
                       AVG(v.duracao) as tempoMedio
                FROM Visita v
                WHERE v.dataAcesso IS NOT NULL
                GROUP BY DATE(v.dataAcesso)
                ORDER BY DATE(v.dataAcesso) DESC
                """;

        return entityManager.createQuery(query, Object[].class)
                .getResultStream()
                .map(result -> new Visita.VisitaDiaria(
                        result[0].toString(),
                        (Long) result[1],
                        result[2] != null ? Math.round((Double) result[2]) : 0L))
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getVisitasPorPagina() {
        String query = """
                SELECT v.pagina,
                       COUNT(v.id) as visitas,
                       AVG(v.duracao) as tempoMedio
                FROM Visita v
                WHERE v.pagina IS NOT NULL
                GROUP BY v.pagina
                ORDER BY visitas DESC
                """;

        return entityManager.createQuery(query, Object[].class)
                .getResultStream()
                .map(result -> Map.of(
                        "pagina", result[0],
                        "visitas", result[1],
                        "tempoMedio", result[2] != null ? Math.round((Double) result[2]) : 0L))
                .collect(Collectors.toList());
    }
}
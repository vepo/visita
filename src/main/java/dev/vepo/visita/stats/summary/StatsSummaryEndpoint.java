package dev.vepo.visita.stats.summary;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import dev.vepo.visita.dashboard.Selector;
import dev.vepo.visita.dashboard.StatsRepository;
import dev.vepo.visita.shared.security.RequiredRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/api/stats/summary")
@ApplicationScoped
@RolesAllowed({ RequiredRoles.ADMIN, RequiredRoles.STATS_VIEWER })
@Produces(MediaType.APPLICATION_JSON)
public class StatsSummaryEndpoint {
    private static final int TOP_DOMAINS_LIMIT = 5;
    private static final int TOP_PAGES_LIMIT = 5;

    private final StatsRepository statsRepository;

    @Inject
    public StatsSummaryEndpoint(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    @GET
    public StatsSummary summary(@QueryParam("startDate") LocalDate startDate,
                                @QueryParam("endDate") LocalDate endDate) {
        var rangeStart = toDateTime(startDate, true);
        var rangeEnd = toDateTime(endDate, false);
        return statsRepository.buildStatsSummary(Selector.NONE, null, rangeStart, rangeEnd, TOP_DOMAINS_LIMIT, TOP_PAGES_LIMIT);
    }

    private LocalDateTime toDateTime(LocalDate date, boolean startOfDay) {
        if (Objects.isNull(date)) {
            return null;
        }
        if (startOfDay) {
            return date.atStartOfDay();
        }
        return date.plusDays(1).atStartOfDay();
    }
}

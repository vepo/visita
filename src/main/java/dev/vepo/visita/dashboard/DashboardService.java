package dev.vepo.visita.dashboard;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class DashboardService {
    private final StatsRepository statsRepository;
    
    @Inject
    public DashboardService(StatsRepository visitaRepository) {
        this.statsRepository = visitaRepository;
    }
    
    public List<DailyStats> getDailyViews() {
        return statsRepository.findDailyViews();
    }

    public List<PageStats> getPageViews() {
        return getPageViews(LocalDateTime.MIN);
    }

    public List<PageStats> getPageViews(LocalDateTime startDate) {
        return statsRepository.findPageViews(startDate);
    }
}

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

    public List<DailyStats> getDailyViews(String hostname) {
        return statsRepository.findDailyViewsPerHostname(hostname);
    }

    public List<PageStats> getPageViews() {
        return statsRepository.findAllPageViews();
    }

    public List<PageStats> getPageViews(String hostname) {
        return statsRepository.findAllPageViewsByHostname(hostname);
    }

    public List<PageStats> getPageViews(String hostname, LocalDateTime startDate) {
        return statsRepository.findAllPageViewsByHostnameFromDate(hostname, startDate);
    }

    public List<PageStats> getPageViews(LocalDateTime startDate) {
        return statsRepository.findPageViewsFromDate(startDate);
    }
}

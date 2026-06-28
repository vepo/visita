package dev.vepo.visita.stats.summary;

import java.util.List;

public record StatsSummary(long totalViews,
                           int daysInRange,
                           int monitoredPages,
                           List<DomainViewCount> topDomains,
                           List<PageViewCount> topPagesLastWeek) {}

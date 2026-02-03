package dev.vepo.visita.dashboard;

import dev.vepo.visita.page.Page;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record PageStats(String page,
                        Long views,
                        Double avgDuration,
                        Long p70Duration,
                        Long p90Duration) {
    public PageStats(Object page, Object views, Object avgDuration, Object p70Duration, Object p90Duration) {
        this(toUrl((Page) page), (Long) views, (Double) avgDuration, (Long) p70Duration, (Long) p90Duration);
    }

    private static String toUrl(Page page) {
        return page.getDomain().getHostname() + page.getPath();
    }
}

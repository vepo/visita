package dev.vepo.visita.dashboard;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record RefererStats(String referer,
                           Long views,
                           Double avgDuration,
                           Long p70Duration,
                           Long p90Duration) {
    public RefererStats(Object referer, Object views, Object avgDuration, Object p70Duration, Object p90Duration) {
        this((String) referer, (Long) views, (Double) avgDuration, (Long) p70Duration, (Long) p90Duration);
    }
}

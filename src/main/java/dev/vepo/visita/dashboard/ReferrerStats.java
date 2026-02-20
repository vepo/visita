package dev.vepo.visita.dashboard;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record ReferrerStats(String referrer,
                            Long views,
                            Double avgDuration,
                            Long p70Duration,
                            Long p90Duration) {
    public ReferrerStats(Object referrer, Object views, Object avgDuration, Object p70Duration, Object p90Duration) {
        this((String) referrer, (Long) views, (Double) avgDuration, (Long) p70Duration, (Long) p90Duration);
    }
}

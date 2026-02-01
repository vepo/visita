package dev.vepo.visita;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record PageStats(String page,
                        Long views,
                        Double avgDuration,
                        Long p70Duration,
                        Long p90Duration) {
    public PageStats(Object page, Object views, Object avgDuration, Object p70Duration, Object p90Duration) {
        this((String) page, (Long) views, (Double) avgDuration, (Long) p70Duration, (Long) p90Duration);
    }
}

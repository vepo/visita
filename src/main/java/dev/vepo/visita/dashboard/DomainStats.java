package dev.vepo.visita.dashboard;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record DomainStats(String domain,
                          Long views,
                          Double avgDuration,
                          Long p70Duration,
                          Long p90Duration) {
    public DomainStats(Object domain, Object views, Object avgDuration, Object p70Duration, Object p90Duration) {
        this((String) domain, (Long) views, (Double) avgDuration, (Long) p70Duration, (Long) p90Duration);
    }
}

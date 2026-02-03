package dev.vepo.visita.dashboard;

import java.time.LocalDate;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record DailyStats(LocalDate date,
                         Long views,
                         Double avgDuration, 
                         Long p70Duration, 
                         Long p90Duration 
) {
    public DailyStats(Object date, Object views, Object avgDuration, Object p70Duration, Object p90Duration) {
        this((LocalDate) date, (Long) views, (Double) avgDuration, (Long) p70Duration, (Long) p90Duration);
    }
}

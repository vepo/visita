package dev.vepo.visita.dashboard;

import java.time.LocalDate;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record UniqueUsersStats(LocalDate date,
                               Long dailyActiveUsers,
                               Long weeklyActiveUsers,
                               Long monthlyActiveUsers) {
    public UniqueUsersStats(Object[] data) {
        this((LocalDate) data[0], (Long) data[1], (Long) data[2], (Long) data[3]);
    }
}

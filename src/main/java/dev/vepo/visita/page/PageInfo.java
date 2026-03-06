package dev.vepo.visita.page;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record PageInfo(long id, double avgReadingTime, long views) {
    public PageInfo(Object id, Object avgReadingTime, Object views) {
        this((long) id, (double) avgReadingTime, (long) views);
    }
}

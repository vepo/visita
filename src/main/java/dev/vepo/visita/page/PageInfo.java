package dev.vepo.visita.page;

import java.util.Objects;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record PageInfo(long id, double avgReadingTime, long views) {
    private static double defaultDouble(Object value) {
        if (Objects.nonNull(value)) {
            return (double) value;
        } else {
            return 0.0;
        }
    }

    private static long defaultLong(Object value) {
        if (Objects.nonNull(value)) {
            return (long) value;
        } else {
            return 0l;
        }
    }

    public PageInfo(Object id, Object avgReadingTime, Object views) {
        this((long) id, defaultDouble(avgReadingTime), defaultLong(views));
    }
}

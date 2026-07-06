package dev.vepo.visita.page.info;

import dev.vepo.visita.page.PageInfo;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record PageInfoResponse(long id, double avgReadingTime, long views) {

    public PageInfoResponse(PageInfo info) {
        this(info.id(), info.avgReadingTime(), info.views());
    }
}

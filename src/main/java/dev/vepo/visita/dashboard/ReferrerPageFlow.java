package dev.vepo.visita.dashboard;

import dev.vepo.visita.page.Page;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record ReferrerPageFlow(String referrer, String page, Long views) {
    public ReferrerPageFlow(Object referrer, Object page, Object views) {
        this(toReferrerLabel((String) referrer), toPageLabel((Page) page), (Long) views);
    }

    private static String toReferrerLabel(String referrer) {
        if (referrer == null || referrer.isBlank()) {
            return "direct";
        }
        return referrer;
    }

    private static String toPageLabel(Page page) {
        return page.getDomain().getHostname() + page.getPath();
    }
}

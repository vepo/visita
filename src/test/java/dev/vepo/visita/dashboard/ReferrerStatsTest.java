package dev.vepo.visita.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.vepo.infra.Given;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class ReferrerStatsTest {

    @Inject
    private StatsRepository statsRepository;

    @BeforeEach
    void cleanup() {
        Given.cleanDatabase();
    }

    @Test
    void shouldGroupInSessionPageViewsByExternalEntryReferrer() {
        var userId = UUID.randomUUID().toString();
        var tabId = UUID.randomUUID().toString();

        Given.view()
             .withPage("https://blog.vepo.dev/")
             .withReferrer("https://google.com")
             .withUserId(userId)
             .withTabId(tabId)
             .withStart(Instant.now().minusSeconds(20))
             .withLength(30)
             .persist();
        Given.view()
             .withPage("https://blog.vepo.dev/about")
             .withReferrer("https://blog.vepo.dev/")
             .withUserId(userId)
             .withTabId(tabId)
             .withStart(Instant.now().minusSeconds(10))
             .withLength(30)
             .persist();
        Given.view()
             .withPage("https://blog.vepo.dev/contact")
             .withReferrer("https://blog.vepo.dev/about")
             .withUserId(userId)
             .withTabId(tabId)
             .withStart(Instant.now())
             .withLength(30)
             .persist();

        var stats = statsRepository.buildReferrerStats(Selector.DOMAIN, "blog.vepo.dev", null, null);

        assertThat(stats).hasSize(1);
        assertThat(stats.get(0).referrer()).isEqualTo("https://google.com");
        assertThat(stats.get(0).views()).isEqualTo(3L);
    }
}

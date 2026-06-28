package dev.vepo.visita.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.vepo.infra.Given;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class IgnoredPathStatsTest {

    @Inject
    private StatsRepository statsRepository;

    @BeforeEach
    void cleanup() {
        Given.cleanDatabase();
    }

    @Test
    void shouldExcludeIgnoredPathsFromPageStats() {
        Given.domain()
             .withHostname("localhost")
             .withToken("local-dev")
             .withIgnoredPathPatterns(List.of("/admin/.*", "/health"))
             .persist();
        Given.view()
             .withPage("https://localhost/")
             .withLength(30)
             .persist();
        Given.view()
             .withPage("https://localhost/admin/users")
             .withLength(30)
             .persist();
        Given.view()
             .withPage("https://localhost/health")
             .withLength(30)
             .persist();

        var stats = statsRepository.buildPageViews(Selector.DOMAIN, "localhost", null, null);

        assertThat(stats).hasSize(1);
        assertThat(stats.get(0).page()).isEqualTo("localhost/");
        assertThat(stats.get(0).views()).isEqualTo(1L);
    }

    @Test
    void shouldExcludeIgnoredPathsFromDailyStats() {
        Given.domain()
             .withHostname("localhost")
             .withToken("local-dev")
             .withIgnoredPathPatterns(List.of("/health"))
             .persist();
        Given.view()
             .withPage("https://localhost/")
             .withStart(Instant.now().minusSeconds(60))
             .withLength(30)
             .persist();
        Given.view()
             .withPage("https://localhost/health")
             .withStart(Instant.now().minusSeconds(60))
             .withLength(30)
             .persist();

        var stats = statsRepository.buildDailyViews(Selector.DOMAIN, "localhost", null, null);

        assertThat(stats).hasSize(1);
        assertThat(stats.get(0).views()).isEqualTo(1L);
    }

}

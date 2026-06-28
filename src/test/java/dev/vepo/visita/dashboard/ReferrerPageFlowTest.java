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
class ReferrerPageFlowTest {

    @Inject
    private StatsRepository statsRepository;

    @BeforeEach
    void cleanup() {
        Given.cleanDatabase();
    }

    @Test
    void shouldBuildReferrerToPageFlowsUsingExternalEntryReferrer() {
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
             .withReferrer("direct")
             .withLength(30)
             .persist();

        var flows = statsRepository.buildReferrerPageFlows(Selector.NONE, null, null, null);

        assertThat(flows).hasSize(3);
        assertThat(flows)
                         .anySatisfy(flow -> {
                             assertThat(flow.referrer()).isEqualTo("https://google.com");
                             assertThat(flow.page()).isEqualTo("blog.vepo.dev/");
                             assertThat(flow.views()).isEqualTo(1L);
                         })
                         .anySatisfy(flow -> {
                             assertThat(flow.referrer()).isEqualTo("https://google.com");
                             assertThat(flow.page()).isEqualTo("blog.vepo.dev/about");
                             assertThat(flow.views()).isEqualTo(1L);
                         })
                         .anySatisfy(flow -> {
                             assertThat(flow.referrer()).isEqualTo("direct");
                             assertThat(flow.page()).isEqualTo("blog.vepo.dev/contact");
                             assertThat(flow.views()).isEqualTo(1L);
                         });
    }

    @Test
    void shouldFilterReferrerPageFlowsByDomain() {
        Given.view()
             .withPage("https://blog.vepo.dev/")
             .withReferrer("https://google.com")
             .withLength(30)
             .persist();
        Given.view()
             .withPage("https://cursos.vepo.dev/")
             .withReferrer("https://google.com")
             .withLength(30)
             .persist();

        var flows = statsRepository.buildReferrerPageFlows(Selector.DOMAIN, "blog.vepo.dev", null, null);

        assertThat(flows).hasSize(1);
        assertThat(flows.get(0).page()).isEqualTo("blog.vepo.dev/");
    }

    @Test
    void shouldBuildPageNavigationFlowsFromSelectedStartPage() {
        Given.view()
             .withPage("https://blog.vepo.dev/")
             .withReferrer("https://google.com")
             .withLength(30)
             .persist();
        Given.view()
             .withPage("https://blog.vepo.dev/about")
             .withReferrer("https://blog.vepo.dev/")
             .withLength(30)
             .persist();
        Given.view()
             .withPage("https://blog.vepo.dev/contact")
             .withReferrer("https://blog.vepo.dev/about")
             .withLength(30)
             .persist();

        var flowsFromHome = statsRepository.buildPageNavigationFlows(Selector.NONE, null, "blog.vepo.dev/", null, null);

        assertThat(flowsFromHome).hasSize(1);
        assertThat(flowsFromHome.get(0).referrer()).isEqualTo("blog.vepo.dev/");
        assertThat(flowsFromHome.get(0).page()).isEqualTo("blog.vepo.dev/about");
        assertThat(flowsFromHome.get(0).views()).isEqualTo(1L);

        var flowsFromAbout = statsRepository.buildPageNavigationFlows(Selector.NONE, null, "blog.vepo.dev/about", null, null);

        assertThat(flowsFromAbout).hasSize(1);
        assertThat(flowsFromAbout.get(0).page()).isEqualTo("blog.vepo.dev/contact");
    }
}

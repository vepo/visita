package dev.vepo.visita;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.vepo.infra.Given;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class ViewTest {

    @Inject
    private ViewRepository viewRepository;

    @BeforeEach
    void cleanup() {
        Given.cleanDatabase();
    }

    @Test
    void originalViewTest() {
        var userId1 = UUID.randomUUID().toString();
        var userId2 = UUID.randomUUID().toString();
        var tabId1 = UUID.randomUUID().toString();
        var tabId2 = UUID.randomUUID().toString();
        Given.view()
             .withPage("https://blog.vepo.dev/")
             .withReferrer("direct")
             .withUserId(userId1)
             .withTabId(tabId1)
             .withStart(Instant.now()
                               .minusSeconds(30))
             .withLength(30)
             .persist();
        var view2 = Given.view()
                         .withPage("https://blog.vepo.dev/")
                         .withReferrer("www.google.com")
                         .withUserId(userId2)
                         .withTabId(tabId2)
                         .withStart(Instant.now()
                                           .minusSeconds(15))
                         .withLength(30)
                         .persist();
        var view3 = Given.view()
                         .withPage("https://blog.vepo.dev/")
                         .withReferrer("https://blog.vepo.dev/")
                         .withUserId(userId1)
                         .withTabId(tabId1)
                         .withStart(Instant.now())
                         .withLength(30)
                         .persist();
        assertThat(viewRepository.findById(view3.getId())).isNotNull()
                                                          .extracting(View::getOriginalReferrer)
                                                          .isNotNull()
                                                          .isEqualTo("direct");
        assertThat(viewRepository.findById(view2.getId())).isNotNull()
                                                          .extracting(View::getOriginalReferrer)
                                                          .isNotNull()
                                                          .isEqualTo("www.google.com");
    }

    @Test
    void shouldKeepOriginalReferrerIndependentPerTab() {
        var userId = UUID.randomUUID().toString();
        var tabA = UUID.randomUUID().toString();
        var tabB = UUID.randomUUID().toString();

        Given.view()
             .withPage("https://blog.vepo.dev/")
             .withReferrer("direct")
             .withUserId(userId)
             .withTabId(tabA)
             .withStart(Instant.now().minusSeconds(20))
             .withLength(10)
             .persist();
        Given.view()
             .withPage("https://blog.vepo.dev/")
             .withReferrer("https://google.com")
             .withUserId(userId)
             .withTabId(tabB)
             .withStart(Instant.now().minusSeconds(15))
             .withLength(10)
             .persist();

        var selfReferralTabA = Given.view()
                                    .withPage("https://blog.vepo.dev/about")
                                    .withReferrer("https://blog.vepo.dev/")
                                    .withUserId(userId)
                                    .withTabId(tabA)
                                    .withStart(Instant.now().minusSeconds(5))
                                    .withLength(10)
                                    .persist();
        var selfReferralTabB = Given.view()
                                    .withPage("https://blog.vepo.dev/about")
                                    .withReferrer("https://blog.vepo.dev/")
                                    .withUserId(userId)
                                    .withTabId(tabB)
                                    .withStart(Instant.now())
                                    .withLength(10)
                                    .persist();

        assertThat(viewRepository.findById(selfReferralTabA.getId()))
                                                                     .extracting(View::getOriginalReferrer)
                                                                     .isEqualTo("direct");
        assertThat(viewRepository.findById(selfReferralTabB.getId()))
                                                                     .extracting(View::getOriginalReferrer)
                                                                     .isEqualTo("https://google.com");
    }

    @Test
    void shouldNotInheritReferrerFromAnotherDomain() {
        var userId = UUID.randomUUID().toString();
        var tabId = UUID.randomUUID().toString();

        Given.view()
             .withPage("https://cursos.vepo.dev/")
             .withReferrer("https://google.com")
             .withUserId(userId)
             .withTabId(tabId)
             .withStart(Instant.now().minusSeconds(10))
             .withLength(10)
             .persist();

        var blogSelfReferral = Given.view()
                                    .withPage("https://blog.vepo.dev/")
                                    .withReferrer("https://blog.vepo.dev/")
                                    .withUserId(userId)
                                    .withTabId(tabId)
                                    .withStart(Instant.now())
                                    .withLength(10)
                                    .persist();

        assertThat(viewRepository.findById(blogSelfReferral.getId()))
                                                                     .extracting(View::getOriginalReferrer)
                                                                     .isNull();
    }
}

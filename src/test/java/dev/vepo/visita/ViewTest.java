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
public class ViewTest {

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
        var view1 = Given.view()
                         .withPage("https://blog.vepo.dev/")
                         .withReferer("direct")
                         .withUserId(userId1)
                         .withTabId(tabId1)
                         .withStart(Instant.now()
                                           .minusSeconds(30))
                         .withLength(30)
                         .persist();
        var view2 = Given.view()
                         .withPage("https://blog.vepo.dev/")
                         .withReferer("www.google.com")
                         .withUserId(userId2)
                         .withTabId(tabId2)
                         .withStart(Instant.now()
                                           .minusSeconds(15))
                         .withLength(30)
                         .persist();
        var view3 = Given.view()
                         .withPage("https://blog.vepo.dev/")
                         .withReferer("https://blog.vepo.dev/")
                         .withUserId(userId1)
                         .withTabId(tabId1)
                         .withStart(Instant.now())
                         .withLength(30)
                         .persist();
        assertThat(viewRepository.findById(view3.getId())).isNotNull()
                                                          .extracting(View::getOriginalView)
                                                          .isNotNull()
                                                          .extracting(OriginalView::getReferer)
                                                          .isEqualTo("direct");
        assertThat(viewRepository.findById(view2.getId())).isNotNull()
                                                          .extracting(View::getOriginalView)
                                                          .isNotNull()
                                                          .extracting(OriginalView::getReferer)
                                                          .isEqualTo("www.google.com");
    }
}

package dev.vepo.visita;

import java.net.URL;
import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import dev.vepo.infra.Given;
import dev.vepo.infra.StaticServer;
import dev.vepo.infra.ViewSession;
import dev.vepo.infra.WebTest;
import dev.vepo.visita.page.Page;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
@WebTest
class AngularTest {
    @TestHTTPResource("/visita.js")
    URL visitaScriptUrl;

    @Inject
    private ViewRepository visitaRepository;

    @BeforeEach
    void cleanup() {
        Given.cleanDatabase();
        Given.domain()
             .withHostname("localhost")
             .withToken("token")
             .persist();
    }

    @Test
    void angularAppTest(WebDriver driver, ViewSession session) {
        try (var siteServer = StaticServer.serverFor("/angular-app.html");
                var otherSever = StaticServer.serverFor("/other-static-page.html")) {
            driver.get(siteServer.getServerURL());
            Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            session.injectScript(visitaScriptUrl, "token");

            // wait for tracker to initialize (exposes window.VisitaTracker.getVisitaId)
            wait.until(d -> session.isScriptLoaded());

            // initial access should have created a visit
            Assertions.assertThat(visitaRepository.findAll()).hasSize(1);

            // Navigate within SPA to the 'About' route
            var aboutLink = driver.findElement(By.id("link-about"));
            aboutLink.click();

            // wait until the view updates to About and the server records both pages
            wait.until(d -> d.findElement(By.id("page-title")).getText().equals("About"));
            wait.until(d -> hasTrackedSpaPaths(Set.of("/about", "/home"))
                    || hasTrackedSpaPaths(Set.of("/about", "/")));

            // Force a new visita (the tracker exposes an async function; trigger it)
            driver.navigate().to(otherSever.getServerURL());
            wait.until(d -> d.getTitle().equals("Other Test Page"));
            wait.until(d -> d.findElement(By.id("done")).isEnabled());

            var visitas = visitaRepository.findAll();
            Assertions.assertThat(visitas).hasSize(2);
            var paths = visitas.stream()
                               .map(View::getPage)
                               .map(Page::getPath)
                               .collect(Collectors.toSet());
            Assertions.assertThat(paths).contains("/about");
            Assertions.assertThat(paths).containsAnyOf("/home", "/");
        }
    }

    private boolean hasTrackedSpaPaths(Set<String> expectedPaths) {
        var paths = visitaRepository.findAll()
                                    .stream()
                                    .map(View::getPage)
                                    .map(Page::getPath)
                                    .collect(Collectors.toSet());
        return paths.equals(expectedPaths);
    }
}
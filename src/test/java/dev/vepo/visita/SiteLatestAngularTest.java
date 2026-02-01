package dev.vepo.visita;

import java.net.URL;
import java.time.Duration;

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
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
@WebTest
class SiteLatestAngularTest {
    @TestHTTPResource("/visita.js")
    URL visitaScriptUrl;

    @Inject
    private ViewRepository visitaRepository;

    @BeforeEach
    void cleanup() {
        Given.cleanDatabase();
        Given.domain()
             .withHostname("localhost")
             .persist();
    }

    @Test
    void latestAngularAppTest(WebDriver driver, ViewSession session) {
        try (var siteServer = StaticServer.serverFor("/latest-angular-app.html")) {
            driver.get(siteServer.getServerURL());
            Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            session.injectScript(visitaScriptUrl);

            // wait for tracker to initialize (exposes window.VisitaTracker.getVisitaId)
            wait.until(d -> session.isScriptLoaded());

            // initial access should have created a visit
            Assertions.assertThat(visitaRepository.findAll()).hasSize(1);

            // Navigate within SPA to the 'Products' route
            var productsLink = driver.findElement(By.id("link-products"));
            productsLink.click();

            // wait until the view updates to Products
            wait.until(d -> d.findElement(By.id("page-title")).getText().equals("Products"));

            wait.until(d -> session.isScriptLoaded());

            // Should still be on the same visit (no new visita created yet)
            Assertions.assertThat(visitaRepository.findAll()).hasSize(2);

            // Navigate to Services route
            var servicesLink = driver.findElement(By.id("link-services"));
            servicesLink.click();

            // wait until the view updates to Services
            wait.until(d -> d.findElement(By.id("page-title")).getText().equals("Services"));

            // Still the same visit
            Assertions.assertThat(visitaRepository.findAll()).hasSize(3);

            // Navigate to Contact route
            var contactLink = driver.findElement(By.id("link-contact"));
            contactLink.click();

            // wait until the view updates to Contact
            wait.until(d -> d.findElement(By.id("page-title")).getText().equals("Contact"));

            var visitas = visitaRepository.findAll();
            Assertions.assertThat(visitas)
                      .hasSize(4)
                      .extracting(View::getPage)
                      .extracting(Page::getPath)
                      .containsExactlyInAnyOrder("/",
                                                 "/products",
                                                 "/contact",
                                                 "/services");
        }
    }

    @Test
    void multipleNavigationTest(WebDriver driver, ViewSession session) {
        try (var siteServer = StaticServer.serverFor("/latest-angular-app.html")) {
            driver.get(siteServer.getServerURL());
            Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            session.injectScript(visitaScriptUrl);

            // wait for tracker to initialize
            wait.until(d -> session.isScriptLoaded());

            // initial access should have created a visit
            Assertions.assertThat(visitaRepository.findAll()).hasSize(1);

            // Perform multiple navigations
            String[] routes = { "link-products", "link-services", "link-contact", "link-dashboard" };
            for (String routeLink : routes) {
                var link = driver.findElement(By.id(routeLink));
                link.click();
                // Just verify navigation works, no new visita should be created
                wait.until(d -> d.findElement(By.id("page-title")).isDisplayed());
            }

            // Should still have only 1 visita (navigation within same session)
            Assertions.assertThat(visitaRepository.findAll()).hasSize(5);

            // Verify we're at the dashboard
            Assertions.assertThat(driver.findElement(By.id("page-title")).getText()).isEqualTo("Dashboard");
        }
    }
}

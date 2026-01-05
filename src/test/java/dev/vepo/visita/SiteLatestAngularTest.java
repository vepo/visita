package dev.vepo.visita;

import static org.junit.jupiter.api.Assertions.fail;

import java.net.URL;
import java.time.Duration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import dev.vepo.infra.WebTest;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WebTest
class SiteLatestAngularTest {
    @TestHTTPResource("/visita.js")
    URL visitaScriptUrl;

    @BeforeEach
    void cleanup() {
        try {
            QuarkusTransaction.begin();
            Visita.deleteAll();
            QuarkusTransaction.commit();
        } catch (Exception e) {
            QuarkusTransaction.rollback();
            fail("Fail to create transaction!", e);
        }
    }

    @Test
    void latestAngularAppTest(WebDriver driver) {
        driver.get(SiteLatestAngularTest.class.getClassLoader().getResource("/latest-angular-app.html").toString());
        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // inject the tracking script
        ((JavascriptExecutor) driver).executeScript("""
                (function(d) {
                    let script = d.createElement('script');
                    script.type = 'text/javascript';
                    script.async = true;
                    script.src = '%s';
                    d.getElementsByTagName('head')[0].appendChild(script);
                }(document));
                """.formatted(visitaScriptUrl.toString()));

        // wait for tracker to initialize (exposes window.VisitaTracker.getVisitaId)
        wait.until(d -> ((JavascriptExecutor) d)
                .executeScript("return typeof window.VisitaTracker !== 'undefined' && window.VisitaTracker.getVisitaId() !== undefined && window.VisitaTracker.getVisitaId() !== null;")
                .equals(Boolean.TRUE));

        // initial access should have created a visit
        Assertions.assertThat(Visita.findAll().count()).isEqualTo(1);

        // Store the first visita ID
        var firstVisitaId = Visita.<Visita>findAll().firstResult().id;

        // Navigate within SPA to the 'Products' route
        var productsLink = driver.findElement(By.id("link-products"));
        productsLink.click();

        // wait until the view updates to Products
        wait.until(d -> d.findElement(By.id("page-title")).getText().equals("Products"));

        // Should still be on the same visit (no new visita created yet)
        Assertions.assertThat(Visita.findAll().count()).isEqualTo(1);

        // Navigate to Services route
        var servicesLink = driver.findElement(By.id("link-services"));
        servicesLink.click();

        // wait until the view updates to Services
        wait.until(d -> d.findElement(By.id("page-title")).getText().equals("Services"));

        // Still the same visit
        Assertions.assertThat(Visita.findAll().count()).isEqualTo(1);

        // Force a new visita (the tracker exposes an async function; trigger it)
        ((JavascriptExecutor) driver).executeScript("window.VisitaTracker.forceNewVisita();");

        // Wait for the second visita to be persisted
        wait.until(d -> Visita.findAll().count() == 2);

        // Navigate to Contact route
        var contactLink = driver.findElement(By.id("link-contact"));
        contactLink.click();

        // wait until the view updates to Contact
        wait.until(d -> d.findElement(By.id("page-title")).getText().equals("Contact"));

        var visitas = Visita.<Visita>findAll().list();
        Assertions.assertThat(visitas).hasSize(2);

        // second visit should reflect the current URL (contains the contact hash)
        var second = visitas.get(1);
        Assertions.assertThat(second.pagina).contains("#!/contact");

        // first visit should be different from the second visit ID
        Assertions.assertThat(second.id).isNotEqualTo(firstVisitaId);
    }

    @Test
    void multipleNavigationTest(WebDriver driver) {
        driver.get(SiteLatestAngularTest.class.getClassLoader().getResource("/latest-angular-app.html").toString());
        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // inject the tracking script
        ((JavascriptExecutor) driver).executeScript("""
                (function(d) {
                    let script = d.createElement('script');
                    script.type = 'text/javascript';
                    script.async = true;
                    script.src = '%s';
                    d.getElementsByTagName('head')[0].appendChild(script);
                }(document));
                """.formatted(visitaScriptUrl.toString()));

        // wait for tracker to initialize
        wait.until(d -> ((JavascriptExecutor) d)
                .executeScript("return typeof window.VisitaTracker !== 'undefined' && window.VisitaTracker.getVisitaId() !== undefined && window.VisitaTracker.getVisitaId() !== null;")
                .equals(Boolean.TRUE));

        // initial access should have created a visit
        Assertions.assertThat(Visita.findAll().count()).isEqualTo(1);

        // Perform multiple navigations
        String[] routes = { "link-products", "link-services", "link-contact", "link-dashboard" };
        for (String routeLink : routes) {
            var link = driver.findElement(By.id(routeLink));
            link.click();
            // Just verify navigation works, no new visita should be created
            wait.until(d -> d.findElement(By.id("page-title")).isDisplayed());
        }

        // Should still have only 1 visita (navigation within same session)
        Assertions.assertThat(Visita.findAll().count()).isEqualTo(1);

        // Verify we're at the dashboard
        Assertions.assertThat(driver.findElement(By.id("page-title")).getText()).equals("Dashboard");
    }
}

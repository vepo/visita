package dev.vepo.visita;

import java.net.URL;
import java.time.Duration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import dev.vepo.infra.WebTest;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WebTest
class SiteAngularTest {
    @TestHTTPResource("/visita.js")
    URL visitaScriptUrl;

    @Test
    void angularAppTest(WebDriver driver) {
        driver.get(SiteAngularTest.class.getClassLoader().getResource("/angular-app.html").toString());
        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // inject the tracking script (same approach as SiteEstaticoTest)
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

        // Navigate within SPA to the 'About' route
        var aboutLink = driver.findElement(By.id("link-about"));
        aboutLink.click();

        // wait until the view updates to About
        wait.until(d -> d.findElement(By.id("page-title")).getText().equals("About"));

        // Force a new visita (the tracker exposes an async function; trigger it)
        ((JavascriptExecutor) driver).executeScript("window.VisitaTracker.forceNewVisita();");

        // Wait for the second visita to be persisted
        wait.until(d -> Visita.findAll().count() == 2);

        var visitas = Visita.<Visita>findAll().list();
        Assertions.assertThat(visitas).hasSize(2);

        // second visit should reflect the current URL (contains the about hash)
        var second = visitas.get(1);
        Assertions.assertThat(second.pagina).contains("#/about");
    }
}

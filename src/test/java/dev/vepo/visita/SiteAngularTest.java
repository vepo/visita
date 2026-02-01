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
import dev.vepo.infra.ViewSession;
import dev.vepo.infra.WebTest;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
@WebTest
class SiteAngularTest {
    @TestHTTPResource("/visita.js")
    URL visitaScriptUrl;

    @Inject
    private VisitaRepository visitaRepository;

    @BeforeEach
    void cleanup() {
        Given.cleanDatabase();
    }

    @Test
    void angularAppTest(WebDriver driver, ViewSession session) {
        driver.get(SiteAngularTest.class.getClassLoader().getResource("/angular-app.html").toString());
        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        session.injectScript(visitaScriptUrl);

        // wait for tracker to initialize (exposes window.VisitaTracker.getVisitaId)
        wait.until(d -> session.isScriptLoaded());

        // initial access should have created a visit
        Assertions.assertThat(visitaRepository.findAll()).hasSize(1);

        // Navigate within SPA to the 'About' route
        var aboutLink = driver.findElement(By.id("link-about"));
        aboutLink.click();

        // wait until the view updates to About
        wait.until(d -> d.findElement(By.id("page-title")).getText().equals("About"));

        // Force a new visita (the tracker exposes an async function; trigger it)
        driver.navigate().to(SiteEstaticoTest.class.getClassLoader().getResource("/other-static-page.html"));
        wait.until(d -> d.getTitle().equals("Other Test Page"));
        wait.until(d -> d.findElement(By.id("done")).isEnabled());

        var visitas = visitaRepository.findAll();
        Assertions.assertThat(visitas)
                  .hasSize(2)
                  .extracting(View::getPagina)
                  .extracting(path -> path.replaceFirst(".*\\.html", ""))
                  .containsExactlyInAnyOrder("#!/about",
                                             "#!/home");
    }
}
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
class StaticHtmlTest {
    @TestHTTPResource("/visita.js")
    URL visitaScriptUrl;

    @TestHTTPResource("/dashboard")
    URL dashboardUrl;

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
    void noneTest(WebDriver driver, ViewSession session) {
        try (var siteServer = StaticServer.serverFor("/static-page.html");
                var otherServer = StaticServer.serverFor("/other-static-page.html")) {
            driver.get(siteServer.getServerURL());
            Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            var btn = driver.findElement(By.id("button1"));
            wait.until(d -> btn.isDisplayed());
            session.injectScript(visitaScriptUrl, "token");
            wait.until(d -> d.findElement(By.id("done")).isEnabled());
            driver.navigate().to(otherServer.getServerURL());
            wait.until(d -> d.getTitle().equals("Other Test Page"));
            wait.until(d -> d.findElement(By.id("done")).isEnabled());
            Assertions.assertThat(visitaRepository.findAll()).hasSize(1);
            var visita = visitaRepository.findAll().get(0);
            Assertions.assertThat(visita.getLength()).isGreaterThan(3);

            driver.navigate().to(dashboardUrl);
            wait.until(d -> d.findElement(By.id("visitas-por-pagina")).isDisplayed());
        }
    }
}

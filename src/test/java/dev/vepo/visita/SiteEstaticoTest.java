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
import jakarta.enterprise.inject.spi.CDI;

@QuarkusTest
@WebTest
class SiteEstaticoTest {
    @TestHTTPResource("/visita.js")
    URL visitaScriptUrl;

    public static <T> T inject(Class<T> clazz) {
        return CDI.current().select(clazz).get();
    }

    @Test
    void noneTest(WebDriver driver) {
        driver.get(SiteEstaticoTest.class.getClassLoader().getResource("/static-page.html").toString());
        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        var btn = driver.findElement(By.id("button1"));
        wait.until(d -> btn.isDisplayed());
        ((JavascriptExecutor) driver).executeScript("""
                (function(d) {
                    let script = d.createElement('script');
                    script.type = 'text/javascript';
                    script.async = true;
                    script.src = '%s';
                    console.log("Injeting ", script);
                    d.getElementsByTagName('head')[0].appendChild(script);
                }(document));
                """.formatted(visitaScriptUrl.toString()));
        wait.until(d -> d.findElement(By.id("done")).isEnabled());
        driver.navigate().to(SiteEstaticoTest.class.getClassLoader().getResource("/other-static-page.html"));
        wait.until(d -> d.getTitle().equals("Other Test Page"));
        wait.until(d -> d.findElement(By.id("done")).isEnabled());
        Assertions.assertThat(Visita.findAll().count()).isEqualTo(1);
        var visita = Visita.<Visita>findAll().firstResult();
        Assertions.assertThat(visita.duracao).isGreaterThan(3);
    }
}

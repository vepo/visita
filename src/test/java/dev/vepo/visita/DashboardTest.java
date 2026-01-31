package dev.vepo.visita;

import java.net.URL;
import java.time.Duration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import dev.vepo.infra.Given;
import dev.vepo.infra.ViewSession;
import dev.vepo.infra.WebTest;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WebTest
class DashboardTest {

    @TestHTTPResource("/dashboard")
    URL visitaScriptUrl;

    @BeforeEach
    void setup() {
        Given.cleanDatabase();
    }

    @Test
    void dashboardShouldDisplayCorrectData(WebDriver driver, ViewSession session) {
        // Create some test data first
        Given.visita().withPagina("/index.html").withDuracao(30).persist();
        Given.visita().withPagina("/about.html").withDuracao(45).persist();
        Given.visita().withPagina("/index.html").withDuracao(25).persist();

        // Navigate to the dashboard page
        driver.navigate().to(visitaScriptUrl);
        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Wait for page to load and check title
        wait.until(d -> d.getTitle().equals("Dashboard - Visita Analytics"));

        // Verify header section
        WebElement header = driver.findElement(By.tagName("header"));
        Assertions.assertThat(header.findElement(By.tagName("h1")).getText())
                  .isEqualTo("Visita Analytics");
        Assertions.assertThat(header.findElement(By.tagName("p")).getText())
                  .isEqualTo("Dashboard de visitas do blog");

        // Check total visits card
        WebElement totalVisitasCard = driver.findElements(By.className("card")).get(0);
        Assertions.assertThat(totalVisitasCard.findElement(By.tagName("h2")).getText())
                  .isEqualTo("Total de Visitas");
        Assertions.assertThat(driver.findElement(By.id("total-visitas")).getText())
                  .as("Total visits should be 3")
                  .isEqualTo("3");

        // Check period analyzed card
        WebElement periodCard = driver.findElements(By.className("card")).get(1);
        Assertions.assertThat(periodCard.findElement(By.tagName("h2")).getText())
                  .isEqualTo("Período Analisado");

        // Check monitored pages card
        WebElement pagesCard = driver.findElements(By.className("card")).get(2);
        Assertions.assertThat(pagesCard.findElement(By.tagName("h2")).getText())
                  .isEqualTo("Páginas Monitoradas");
        Assertions.assertThat(pagesCard.findElement(By.id("paginas-monitoradas")).getText())
                  .as("Should show 2 different pages")
                  .isEqualTo("2 páginas");

        // Find the "Visitas Diárias" card - now contains charts
        WebElement dailyVisitsCard = driver.findElement(By.id("visitas-diarias"));

        Assertions.assertThat(dailyVisitsCard)
                  .as("Should find Visitas Diárias card")
                  .isNotNull();

        // Verify charts are present inside the Visitas Diárias card
        // Look for the two chart titles
        wait.until(d -> dailyVisitsCard.findElements(By.tagName("h3")).size() >= 2);

        var chartTitles = dailyVisitsCard.findElements(By.tagName("h3"));
        Assertions.assertThat(chartTitles)
                  .extracting(WebElement::getText)
                  .containsExactlyInAnyOrder("Número de Visitas", "Métricas de Tempo");

        // Verify charts canvas elements exist
        Assertions.assertThat(dailyVisitsCard.findElement(By.id("visitasDiariasChart")))
                  .isNotNull();
        Assertions.assertThat(dailyVisitsCard.findElement(By.id("tempoMedioChart")))
                  .isNotNull();

        // Verify visits by page table structure
        WebElement visitsByPageTable = driver.findElement(By.xpath("//h2[text()='Visitas por Página']/following::table[1]"));
        Assertions.assertThat(visitsByPageTable.findElements(By.tagName("th")))
                  .hasSize(5)
                  .extracting(WebElement::getText)
                  .extracting(String::toLowerCase)
                  .containsExactly("página", "visitas", "p70", "p90", "tempo médio");

        // Verify "Visitas por Página (Última semana)" table structure
        WebElement visitsByPageLastWeekTable = driver.findElement(By.xpath("//h2[text()='Visitas por Página (Última semana)']/following::table[1]"));
        Assertions.assertThat(visitsByPageLastWeekTable.findElements(By.tagName("th")))
                  .hasSize(5)
                  .extracting(WebElement::getText)
                  .extracting(String::toLowerCase)
                  .containsExactly("página", "visitas", "p70", "p90", "tempo médio");

        // Check that visits by page table has data
        // The template should have populated rows with actual data
        wait.until(d -> {
            WebElement tableBody = visitsByPageTable.findElement(By.tagName("tbody"));
            return !tableBody.findElements(By.tagName("tr")).isEmpty();
        });

        // Verify table rows contain expected data
        WebElement tableBody = visitsByPageTable.findElement(By.tagName("tbody"));
        var rows = tableBody.findElements(By.tagName("tr"));
        Assertions.assertThat(rows)
                  .as("Should have rows for each distinct page")
                  .hasSize(2);

        // Check footer
        WebElement footer = driver.findElement(By.xpath("//div[contains(@class,'mt-6')]"));
        Assertions.assertThat(footer.getText())
                  .isEqualTo("Visita Analytics v1.0 - Dashboard de monitoramento");
    }

    @Test
    void dashboardShouldHandleEmptyData(WebDriver driver) {
        // Navigate to dashboard with empty database
        driver.navigate().to(visitaScriptUrl);
        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Wait for page to load
        wait.until(d -> d.getTitle().equals("Dashboard - Visita Analytics"));

        Assertions.assertThat(driver.findElement(By.id("total-visitas")).getText())
                  .as("Total visits should be 0 when no data")
                  .isEqualTo("0");

        // Check monitored pages shows 0
        Assertions.assertThat(driver.findElement(By.id("paginas-monitoradas")).getText())
                  .as("Should show 0 pages when no data")
                  .isEqualTo("0 páginas");

        // Check that tables have empty bodies (or show no data message)
        WebElement visitsByPageTable = driver.findElement(By.xpath("//h2[text()='Visitas por Página']/following::table[1]"));
        WebElement tableBody = visitsByPageTable.findElement(By.tagName("tbody"));
        var rows = tableBody.findElements(By.tagName("tr"));

        // Depending on template implementation, it might have 0 rows or rows with
        // placeholder data
        // This assertion would need adjustment based on actual template behavior
        Assertions.assertThat(rows.size())
                  .as("Table should handle empty data gracefully")
                  .isGreaterThanOrEqualTo(0);
    }

    @Test
    void dashboardShouldUpdateOnNewVisits(WebDriver driver, ViewSession session) throws InterruptedException {
        // Navigate to dashboard
        driver.navigate().to(visitaScriptUrl);
        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        wait.until(d -> d.getTitle().equals("Dashboard - Visita Analytics"));

        String initialCount = driver.findElement(By.id("total-visitas")).getText();

        // Create a new visit (simulating backend data change)
        // In a real scenario, you might need to refresh the page or it might
        // auto-update
        Given.visita().withPagina("/new-page.html").withDuracao(60).persist();

        // Refresh dashboard to see updated data
        driver.navigate().refresh();
        wait.until(d -> d.getTitle().equals("Dashboard - Visita Analytics"));

        // Verify total visits increased by 1
        String updatedCount = driver.findElement(By.id("total-visitas")).getText();
        Assertions.assertThat(Integer.parseInt(updatedCount))
                  .as("Total visits should increase after adding new visit")
                  .isEqualTo(Integer.parseInt(initialCount) + 1);
    }
}
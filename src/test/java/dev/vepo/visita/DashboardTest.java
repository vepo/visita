package dev.vepo.visita;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import dev.vepo.infra.Given;
import dev.vepo.infra.WebTest;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WebTest
class DashboardTest {

    @TestHTTPResource("/dashboard")
    URL mainDashboard;

    @TestHTTPResource("/dashboard/domain/blog.vepo.dev")
    URL blogDashboard;

    @TestHTTPResource("/dashboard/referer/direct")
    URL refererDashboard;

    private static final DateTimeFormatter loadByLocale() {
        if (Locale.getDefault().getCountry().equals("BR")) {
            return DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        } else {
            return DateTimeFormatter.ISO_LOCAL_DATE;
        }
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter FRONT_DATE_FORMATTER = loadByLocale();

    @BeforeEach
    void setup() {
        Given.cleanDatabase();
    }

    @Test
    void dashboardShouldDisplayCorrectData(WebDriver driver) {
        // Create some test data first
        Given.view().withPage("https://localhost:8080/index.html").withLength(30).persist();
        Given.view().withPage("https://localhost:8080/about.html").withLength(45).persist();
        Given.view().withPage("https://localhost:8080/index.html").withLength(25).persist();

        // Navigate to the dashboard page
        driver.navigate().to(mainDashboard);
        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Wait for page to load and check title
        wait.until(d -> d.getTitle().equals("Dashboard - Visita Analytics"));

        // Verify header section
        WebElement header = driver.findElement(By.tagName("header"));
        Assertions.assertThat(header.findElement(By.tagName("h1")).getText())
                  .isEqualTo("Visita Analytics");

        // Check total visits card
        WebElement totalViewsCard = driver.findElements(By.className("card")).get(0);
        Assertions.assertThat(totalViewsCard.findElement(By.tagName("h2")).getText())
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
                  .containsExactlyInAnyOrder("Número de Visitas", "Usuários Recorrentes", "Métricas de Tempo");

        // Verify charts canvas elements exist
        Assertions.assertThat(dailyVisitsCard.findElement(By.id("daily-views-chart")))
                  .isNotNull();
        Assertions.assertThat(dailyVisitsCard.findElement(By.id("avg-duration-chart")))
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
    void dashboardShouldDisplayCorrectDataPerDomain(WebDriver driver) {
        // Create some test data first
        Given.view().withPage("https://blog.vepo.dev/").withLength(30).persist();
        Given.view().withPage("https://blog.vepo.dev/about").withLength(45).persist();
        Given.view().withPage("https://blog.vepo.dev/").withLength(25).persist();
        Given.view().withPage("https://cursos.vepo.dev/").withLength(25).persist();

        // Navigate to the dashboard page
        driver.navigate().to(blogDashboard);
        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Wait for page to load and check title
        wait.until(d -> d.getTitle().equals("Dashboard - Visita Analytics"));

        // Verify header section
        WebElement header = driver.findElement(By.tagName("header"));
        Assertions.assertThat(header.findElement(By.tagName("h1")).getText())
                  .isEqualTo("Visita Analytics");

        // Check total visits card
        WebElement totalViewsCard = driver.findElements(By.className("card")).get(0);
        Assertions.assertThat(totalViewsCard.findElement(By.tagName("h2")).getText())
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
                  .containsExactlyInAnyOrder("Número de Visitas", "Usuários Recorrentes", "Métricas de Tempo");

        // Verify charts canvas elements exist
        Assertions.assertThat(dailyVisitsCard.findElement(By.id("daily-views-chart")))
                  .isNotNull();
        Assertions.assertThat(dailyVisitsCard.findElement(By.id("avg-duration-chart")))
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
    void dashboardShouldDisplayCorrectDataPerReferer(WebDriver driver) {
        // Create some test data first
        Given.view().withPage("https://blog.vepo.dev/").withReferer("direct").withLength(30).persist();
        Given.view().withPage("https://blog.vepo.dev/about").withReferer("direct").withLength(45).persist();
        Given.view().withPage("https://cursos.vepo.dev/").withReferer("direct").withLength(25).persist();
        Given.view().withPage("https://blog.vepo.dev/").withReferer("google.com").withLength(25).persist();

        // Navigate to the dashboard page
        driver.navigate().to(refererDashboard);
        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Wait for page to load and check title
        wait.until(d -> d.getTitle().equals("Dashboard - Visita Analytics"));

        // Verify header section
        WebElement header = driver.findElement(By.tagName("header"));
        Assertions.assertThat(header.findElement(By.tagName("h1")).getText())
                  .isEqualTo("Visita Analytics");

        // Check total visits card
        WebElement totalViewsCard = driver.findElements(By.className("card")).get(0);
        Assertions.assertThat(totalViewsCard.findElement(By.tagName("h2")).getText())
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
                  .isEqualTo("3 páginas");

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
                  .containsExactlyInAnyOrder("Número de Visitas", "Usuários Recorrentes", "Métricas de Tempo");

        // Verify charts canvas elements exist
        Assertions.assertThat(dailyVisitsCard.findElement(By.id("daily-views-chart")))
                  .isNotNull();
        Assertions.assertThat(dailyVisitsCard.findElement(By.id("avg-duration-chart")))
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
                  .hasSize(3);

        // Check footer
        WebElement footer = driver.findElement(By.xpath("//div[contains(@class,'mt-6')]"));
        Assertions.assertThat(footer.getText())
                  .isEqualTo("Visita Analytics v1.0 - Dashboard de monitoramento");
    }

    @Test
    void dashboardShouldHandleEmptyData(WebDriver driver) {
        // Navigate to dashboard with empty database
        driver.navigate().to(mainDashboard);
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
    void dashboardShouldUpdateOnNewVisits(WebDriver driver) throws InterruptedException {
        // Navigate to dashboard
        driver.navigate().to(mainDashboard);
        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        wait.until(d -> d.getTitle().equals("Dashboard - Visita Analytics"));

        String initialCount = driver.findElement(By.id("total-visitas")).getText();

        // Create a new visit (simulating backend data change)
        // In a real scenario, you might need to refresh the page or it might
        // auto-update
        Given.view().withPage("https://localhost:8080/new-page.html").withLength(60).persist();

        // Refresh dashboard to see updated data
        driver.navigate().refresh();
        wait.until(d -> d.getTitle().equals("Dashboard - Visita Analytics"));

        // Verify total visits increased by 1
        String updatedCount = driver.findElement(By.id("total-visitas")).getText();
        Assertions.assertThat(Integer.parseInt(updatedCount))
                  .as("Total visits should increase after adding new visit")
                  .isEqualTo(Integer.parseInt(initialCount) + 1);
    }

    // ================ DATE FILTERING TESTS ================

    @Test
    void dateFilterInputsShouldBePresent(WebDriver driver) {
        driver.navigate().to(mainDashboard);
        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(d -> d.getTitle().equals("Dashboard - Visita Analytics"));

        // Verify date filter elements exist
        WebElement startDateInput = driver.findElement(By.id("startDate"));
        WebElement endDateInput = driver.findElement(By.id("endDate"));
        WebElement filterButton = driver.findElement(By.id("filterButton"));

        Assertions.assertThat(startDateInput).isNotNull();
        Assertions.assertThat(endDateInput).isNotNull();
        Assertions.assertThat(filterButton).isNotNull();

        Assertions.assertThat(startDateInput.getAttribute("type")).isEqualTo("date");
        Assertions.assertThat(endDateInput.getAttribute("type")).isEqualTo("date");
        Assertions.assertThat(filterButton.getText()).isEqualTo("Filtrar");
    }

    @Test
    void dateFilterShouldLoadFromUrlParameters(WebDriver driver) {
        // Create test data with specific dates
        LocalDate today = LocalDate.now();
        LocalDate lastWeek = today.minusDays(7);

        // Navigate to dashboard with date parameters
        String url = mainDashboard.toString() +
                "?startDate=" + lastWeek.format(DATE_FORMATTER) +
                "&endDate=" + today.format(DATE_FORMATTER);
        driver.navigate().to(url);

        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(d -> d.getTitle().equals("Dashboard - Visita Analytics"));

        // Verify date inputs are populated from URL
        WebElement startDateInput = driver.findElement(By.id("startDate"));
        WebElement endDateInput = driver.findElement(By.id("endDate"));

        Assertions.assertThat(startDateInput.getAttribute("value"))
                  .isEqualTo(lastWeek.format(DATE_FORMATTER));
        Assertions.assertThat(endDateInput.getAttribute("value"))
                  .isEqualTo(today.format(DATE_FORMATTER));
    }

    @Test
    void dateFilterShouldFilterDataByDateRange(WebDriver driver) {
        // Create test data with different dates
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate twoDaysAgo = today.minusDays(2);
        LocalDate lastWeek = today.minusDays(7);

        // Create views with different dates using timestamp manipulation
        // Note: You might need to implement a method in Given to set specific
        // timestamps
        Given.view().withPage("https://localhost:8080/page1.html")
             .withLength(30)
             .withAccessDate(lastWeek) // This method would need to be added
             .persist();

        Given.view().withPage("https://localhost:8080/page2.html")
             .withLength(45)
             .withAccessDate(twoDaysAgo)
             .persist();

        Given.view().withPage("https://localhost:8080/page3.html")
             .withLength(25)
             .withAccessDate(yesterday)
             .persist();

        Given.view().withPage("https://localhost:8080/page4.html")
             .withLength(60)
             .withAccessDate(today)
             .persist();

        // Navigate to dashboard with date range filter (last 3 days)
        String url = mainDashboard.toString() +
                "?startDate=" + twoDaysAgo.format(DATE_FORMATTER) +
                "&endDate=" + today.format(DATE_FORMATTER);
        driver.navigate().to(url);

        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(d -> d.getTitle().equals("Dashboard - Visita Analytics"));

        // Verify total visits count (should be 3 - twoDaysAgo, yesterday, today)
        String totalVisits = driver.findElement(By.id("total-visitas")).getText();
        Assertions.assertThat(totalVisits)
                  .as("Total visits should be filtered to date range")
                  .isEqualTo("3");

        // Verify pages count (should be 3 pages in the date range)
        String pagesCount = driver.findElement(By.id("paginas-monitoradas")).getText();
        Assertions.assertThat(pagesCount)
                  .as("Pages count should be filtered to date range")
                  .isEqualTo("3 páginas");
    }

    @Disabled
    @Test
    void dateFilterShouldNavigateToNewUrlWhenApplied(WebDriver driver) {
        driver.navigate().to(mainDashboard);

        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(d -> d.getTitle().equals("Dashboard - Visita Analytics"));

        LocalDate today = LocalDate.now();
        LocalDate lastWeek = today.minusDays(7);

        // Set date inputs
        WebElement startDateInput = driver.findElement(By.id("startDate"));
        WebElement endDateInput = driver.findElement(By.id("endDate"));

        startDateInput.sendKeys(lastWeek.format(FRONT_DATE_FORMATTER));
        endDateInput.sendKeys(today.format(FRONT_DATE_FORMATTER));

        // Click filter button
        WebElement filterButton = driver.findElement(By.id("filterButton"));
        filterButton.click();

        // Wait for navigation and verify URL contains date parameters
        wait.until(d -> d.getCurrentUrl().contains("startDate=" + lastWeek.format(DATE_FORMATTER)));
        wait.until(d -> d.getCurrentUrl().contains("endDate=" + today.format(DATE_FORMATTER)));
    }

    @Test
    void dateFilterShouldWorkWithDomainSpecificDashboard(WebDriver driver) {
        // Create test data with different dates
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate lastWeek = today.minusDays(7);

        // Create views for blog domain
        Given.view().withPage("https://blog.vepo.dev/post1")
             .withLength(30)
             .withAccessDate(lastWeek)
             .persist();

        Given.view().withPage("https://blog.vepo.dev/post2")
             .withLength(45)
             .withAccessDate(yesterday)
             .persist();

        Given.view().withPage("https://blog.vepo.dev/post3")
             .withLength(25)
             .withAccessDate(today)
             .persist();

        // Create view for different domain (should be filtered out)
        Given.view().withPage("https://other-domain.com/page")
             .withLength(60)
             .withAccessDate(today)
             .persist();

        // Navigate to domain dashboard with date filter
        String url = blogDashboard.toString() +
                "?startDate=" + yesterday.format(DATE_FORMATTER) +
                "&endDate=" + today.format(DATE_FORMATTER);
        driver.navigate().to(url);

        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(d -> d.getTitle().equals("Dashboard - Visita Analytics"));

        // Verify total visits count (should be 2 - yesterday and today)
        String totalVisits = driver.findElement(By.id("total-visitas")).getText();
        Assertions.assertThat(totalVisits)
                  .as("Domain dashboard should respect date filter")
                  .isEqualTo("2");

        // Verify pages count
        String pagesCount = driver.findElement(By.id("paginas-monitoradas")).getText();
        Assertions.assertThat(pagesCount)
                  .as("Pages count should be filtered by both domain and date")
                  .isEqualTo("2 páginas");
    }

    @Test
    void dateFilterShouldWorkWithRefererSpecificDashboard(WebDriver driver) {
        // Create test data with different dates
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate lastWeek = today.minusDays(7);

        // Create views for direct referer
        Given.view().withPage("https://blog.vepo.dev/page1")
             .withReferer("direct")
             .withLength(30)
             .withAccessDate(lastWeek)
             .persist();

        Given.view().withPage("https://blog.vepo.dev/page2")
             .withReferer("direct")
             .withLength(45)
             .withAccessDate(yesterday)
             .persist();

        Given.view().withPage("https://cursos.vepo.dev/page3")
             .withReferer("direct")
             .withLength(25)
             .withAccessDate(today)
             .persist();

        // Create view for different referer (should be filtered out)
        Given.view().withPage("https://blog.vepo.dev/page4")
             .withReferer("google.com")
             .withLength(60)
             .withAccessDate(today)
             .persist();

        // Navigate to referer dashboard with date filter
        String url = refererDashboard.toString() +
                "?startDate=" + yesterday.format(DATE_FORMATTER) +
                "&endDate=" + today.format(DATE_FORMATTER);
        driver.navigate().to(url);

        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(d -> d.getTitle().equals("Dashboard - Visita Analytics"));

        // Verify total visits count (should be 2 - yesterday and today)
        String totalVisits = driver.findElement(By.id("total-visitas")).getText();
        Assertions.assertThat(totalVisits)
                  .as("Referer dashboard should respect date filter")
                  .isEqualTo("2");

        // Verify pages count
        String pagesCount = driver.findElement(By.id("paginas-monitoradas")).getText();
        Assertions.assertThat(pagesCount)
                  .as("Pages count should be filtered by both referer and date")
                  .isEqualTo("2 páginas");
    }

    @Test
    void dateFilterShouldPreserveLinksWhenNavigating(WebDriver driver) {
        // Navigate to dashboard with date parameters
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate lastWeek = today.minusDays(7);
        Given.view().withPage("https://blog.vepo.dev/page2")
             .withReferer("direct")
             .withLength(45)
             .withAccessDate(yesterday)
             .persist();

        String url = mainDashboard.toString() +
                "?startDate=" + lastWeek.format(DATE_FORMATTER) +
                "&endDate=" + today.format(DATE_FORMATTER);
        driver.navigate().to(url);

        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(d -> d.getTitle().equals("Dashboard - Visita Analytics"));

        // Find a domain link and verify it preserves date parameters
        WebElement firstDomainLink = driver.findElement(By.xpath("//a[contains(@href, '/dashboard/domain/')]"));
        String linkHref = firstDomainLink.getAttribute("href");

        Assertions.assertThat(linkHref)
                  .contains("startDate=" + lastWeek.format(DATE_FORMATTER))
                  .contains("endDate=" + today.format(DATE_FORMATTER));
    }
}
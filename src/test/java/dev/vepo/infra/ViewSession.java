package dev.vepo.infra;

import java.net.URL;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public class ViewSession {

    private final WebDriver driver;

    public ViewSession(WebDriver driver) {
        this.driver = driver;
    }

    public boolean isScriptLoaded() {
        return ((JavascriptExecutor) driver).executeScript("return typeof window.VisitaAnalytics !== 'undefined' && window.VisitaAnalytics.getSessionId() !== undefined && window.VisitaAnalytics.getSessionId() !== null;")
                                            .equals(Boolean.TRUE);
    }

    public void injectScript(URL visitaScriptUrl, String token) {
        // inject the tracking script
        ((JavascriptExecutor) driver).executeScript("""
                                                    (function(d) {
                                                        let script = d.createElement('script');
                                                        script.type = 'text/javascript';
                                                        script.async = true;
                                                        script.setAttribute('data-token', '%s');
                                                        script.src = '%s';
                                                        d.getElementsByTagName('head')[0].appendChild(script);
                                                    }(document));
                                                    """.formatted(token, visitaScriptUrl.toString()));
    }

}

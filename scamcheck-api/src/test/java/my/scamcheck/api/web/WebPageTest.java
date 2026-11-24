package my.scamcheck.api.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** The static page is served, and every response carries the security headers (NFR-05). */
@SpringBootTest
@AutoConfigureMockMvc
class WebPageTest {

    @Autowired
    private MockMvcTester mvc;

    @Test
    void rootServesTheWelcomePage() {
        assertThat(mvc.get().uri("/")).hasStatusOk().hasForwardedUrl("index.html");
    }

    @Test
    void pageAndAssetsAreServed() {
        assertThat(mvc.get().uri("/index.html")).hasStatusOk()
                .hasContentTypeCompatibleWith(MediaType.TEXT_HTML)
                .bodyText().contains("<script src=\"app.js\" defer></script>");
        assertThat(mvc.get().uri("/app.js")).hasStatusOk();
        assertThat(mvc.get().uri("/app.css")).hasStatusOk();
        assertThat(mvc.get().uri("/i18n/en.json")).hasStatusOk();
        assertThat(mvc.get().uri("/i18n/ms.json")).hasStatusOk();
    }

    @Test
    void securityHeadersOnPageAndApi() {
        MvcTestResult page = mvc.get().uri("/index.html").exchange();
        MvcTestResult api = mvc.post().uri("/api/v1/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\": \"hello\"}")
                .exchange();

        for (MvcTestResult result : new MvcTestResult[] {page, api}) {
            assertThat(result).headers()
                    .hasValue("Content-Security-Policy", SecurityHeadersFilter.CONTENT_SECURITY_POLICY)
                    .hasValue("X-Content-Type-Options", "nosniff")
                    .hasValue("Referrer-Policy", "no-referrer")
                    .hasValue("X-Frame-Options", "DENY");
        }
    }
}

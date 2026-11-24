package my.scamcheck.api.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * Static checks on the web page that a browser test would only catch later:
 * both UI languages have the same text keys, every key the page uses exists,
 * and the page follows the CSP and XSS rules of ADR-006.
 */
class StaticPageGuardTest {

    private static final Pattern HTML_KEY = Pattern.compile("data-i18n(?:-placeholder)?=\"([^\"]+)\"");
    private static final Pattern JS_KEY = Pattern.compile("\\bt\\('([\\w.]+)'");
    /** Keys built at runtime in app.js, e.g. t(`riskLabel.${level}`). */
    private static final List<String> DYNAMIC_KEYS = List.of(
            "riskLabel.HIGH", "riskLabel.MEDIUM", "riskLabel.LOW",
            "strength.STRONG", "strength.MODERATE", "strength.WEAK", "strength.INFO",
            "errorEmpty", "errorTooLong", "errorInvalid", "errorServer", "errorNetwork",
            "check", "checking", "pageTitle", "counter", "exampleText");

    @Test
    void bothLanguagesHaveTheSameNonBlankKeys() throws IOException { // SRS US-09 AC2
        Map<String, Object> en = json("/static/i18n/en.json");
        Map<String, Object> ms = json("/static/i18n/ms.json");

        assertThat(ms.keySet()).containsExactlyInAnyOrderElementsOf(en.keySet());
        assertThat(en.values()).allSatisfy(value -> assertThat(String.valueOf(value)).isNotBlank());
        assertThat(ms.values()).allSatisfy(value -> assertThat(String.valueOf(value)).isNotBlank());
    }

    @Test
    void everyKeyThePageUsesExists() throws IOException {
        Set<String> used = new LinkedHashSet<>(DYNAMIC_KEYS);
        used.addAll(matches(HTML_KEY, text("/static/index.html")));
        used.addAll(matches(JS_KEY, text("/static/app.js")));

        Set<String> defined = json("/static/i18n/en.json").keySet();
        assertThat(defined).containsAll(used);
    }

    @Test
    void pageHasNoInlineScriptsOrStyles() throws IOException { // CSP: script-src/style-src 'self'
        String html = text("/static/index.html");

        assertThat(html).doesNotContainPattern("<script(?![^>]*\\bsrc=)[^>]*>");
        assertThat(html).doesNotContain("<style").doesNotContainPattern("\\sstyle=\"").doesNotContainPattern("\\son\\w+=\"");
    }

    @Test
    void scriptNeverInsertsHtml() throws IOException { // NFR-05: text only, never markup
        String js = text("/static/app.js").replaceAll("(?s)/\\*.*?\\*/", ""); // ignore the rules comment

        assertThat(js).doesNotContain("innerHTML", "outerHTML", "insertAdjacentHTML", "document.write", "eval(");
    }

    @Test
    void scriptNeverLogsOrStoresTheInput() throws IOException { // ADR-008
        String js = text("/static/app.js");

        assertThat(js).doesNotContain("console.");
        assertThat(js).doesNotContainPattern("localStorage\\.setItem\\((?!STORAGE_KEY, lang\\))");
    }

    private static Set<String> matches(Pattern pattern, String text) {
        Set<String> found = new LinkedHashSet<>();
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            found.add(matcher.group(1));
        }
        return found;
    }

    private static Map<String, Object> json(String resource) throws IOException {
        return JsonPath.parse(text(resource)).read("$");
    }

    private static String text(String resource) throws IOException {
        try (InputStream in = StaticPageGuardTest.class.getResourceAsStream(resource)) {
            assertThat(in).as(resource).isNotNull();
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}

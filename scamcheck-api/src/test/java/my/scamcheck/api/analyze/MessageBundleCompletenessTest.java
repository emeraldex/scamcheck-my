package my.scamcheck.api.analyze;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import my.scamcheck.core.ScamAnalyzer;
import my.scamcheck.core.model.RiskLevel;
import my.scamcheck.core.rule.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Every message the API can produce exists in both languages (ADR-009, SRS US-09 AC2).
 * Uses the real wired analyzer, so a newly registered rule without messages fails here.
 */
@SpringBootTest
class MessageBundleCompletenessTest {

    @Autowired
    private ScamAnalyzer analyzer;

    @Test
    void everyRequiredKeyExistsInEnglishAndMalay() throws IOException {
        Properties en = load("messages_en.properties");
        Properties ms = load("messages_ms.properties");

        for (String key : requiredKeys()) {
            assertThat(en.getProperty(key)).as("en: %s", key).isNotBlank();
            assertThat(ms.getProperty(key)).as("ms: %s", key).isNotBlank();
        }
    }

    @Test
    void bothBundlesHaveExactlyTheSameKeys() throws IOException {
        Set<String> enKeys = load("messages_en.properties").stringPropertyNames();
        Set<String> msKeys = load("messages_ms.properties").stringPropertyNames();

        assertThat(msKeys).containsExactlyInAnyOrderElementsOf(enKeys);
    }

    private List<String> requiredKeys() {
        List<String> keys = new ArrayList<>(List.of(
                "summary.LOW.minor", "help.semak-mule.label", "help.nsrc-997.label", "disclaimer"));
        for (RiskLevel level : RiskLevel.values()) {
            keys.add("summary." + level);
            keys.add("tip." + level);
        }
        for (Rule rule : analyzer.rules()) {
            keys.add("finding." + rule.id() + ".title");
            keys.add("finding." + rule.id() + ".explanation");
        }
        return keys;
    }

    private static Properties load(String resource) throws IOException {
        try (InputStream in = MessageBundleCompletenessTest.class.getResourceAsStream("/" + resource)) {
            assertThat(in).as(resource).isNotNull();
            Properties properties = new Properties();
            properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            return properties;
        }
    }
}

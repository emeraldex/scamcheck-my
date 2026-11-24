package my.scamcheck.core.rule.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Reads one-value-per-line data files such as {@code url-shorteners.txt} (ADR-007).
 * Values are lower-cased and trimmed; {@code #} starts a comment. Duplicates and
 * empty files are errors, so a bad data change fails the build.
 */
public final class SimpleListLoader {

    private SimpleListLoader() {
    }

    /** Loads a classpath resource, e.g. {@code /rules/url-shorteners.txt}. */
    public static Set<String> loadResource(String resourcePath) {
        return parse(resourcePath, readLines(resourcePath));
    }

    public static Set<String> parse(String source, List<String> lines) {
        Set<String> values = new LinkedHashSet<>();
        for (int i = 0; i < lines.size(); i++) {
            String value = lines.get(i).strip().toLowerCase(Locale.ROOT);
            if (value.isEmpty() || value.startsWith("#")) {
                continue;
            }
            if (value.chars().anyMatch(Character::isWhitespace)) {
                throw new RuleDataException(source + ":" + (i + 1) + ": value must not contain spaces");
            }
            if (!values.add(value)) {
                throw new RuleDataException(source + ":" + (i + 1) + ": duplicate value '" + value + "'");
            }
        }
        if (values.isEmpty()) {
            throw new RuleDataException(source + ": no values found");
        }
        return Set.copyOf(values);
    }

    static List<String> readLines(String resourcePath) {
        try (InputStream in = SimpleListLoader.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new RuleDataException("Rule data file not found: " + resourcePath);
            }
            return new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)).lines().toList();
        } catch (IOException e) {
            throw new RuleDataException("Could not read rule data file: " + resourcePath, e);
        }
    }
}

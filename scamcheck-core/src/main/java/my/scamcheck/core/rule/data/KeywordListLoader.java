package my.scamcheck.core.rule.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import my.scamcheck.core.model.Weight;
import my.scamcheck.core.rule.KeywordEntry;

/**
 * Reads keyword data files (ADR-007). Format, one entry per line:
 *
 * <pre>
 * # comment
 * STRONG   | ms | dijamin untung
 * </pre>
 *
 * <p>Any problem (wrong number of fields, unknown weight or language, duplicate
 * phrase, empty file) throws {@link RuleDataException} naming the file and
 * line, so a bad data change fails the build instead of failing silently.
 */
public final class KeywordListLoader {

    private static final Set<String> LANGUAGES = Set.of("en", "ms");

    private KeywordListLoader() {
    }

    /** Loads a classpath resource, e.g. {@code /rules/keywords/urgency.txt}. */
    public static List<KeywordEntry> loadResource(String resourcePath) {
        try (InputStream in = KeywordListLoader.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new RuleDataException("Rule data file not found: " + resourcePath);
            }
            var reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            return parse(resourcePath, reader.lines().toList());
        } catch (IOException e) {
            throw new RuleDataException("Could not read rule data file: " + resourcePath, e);
        }
    }

    /** Parses lines already read from {@code source}; {@code source} is used in error messages. */
    public static List<KeywordEntry> parse(String source, List<String> lines) {
        List<KeywordEntry> entries = new ArrayList<>();
        Set<String> seenPhrases = new HashSet<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).strip();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            KeywordEntry entry = parseLine(source, i + 1, line);
            if (!seenPhrases.add(entry.phrase())) {
                throw error(source, i + 1, "duplicate phrase '" + entry.phrase() + "'");
            }
            entries.add(entry);
        }

        if (entries.isEmpty()) {
            throw new RuleDataException(source + ": no keywords found");
        }
        return List.copyOf(entries);
    }

    private static KeywordEntry parseLine(String source, int lineNumber, String line) {
        String[] fields = line.split("\\|", -1);
        if (fields.length != 3) {
            throw error(source, lineNumber, "expected 'WEIGHT | language | phrase' but found " + fields.length + " field(s)");
        }
        Weight weight = parseWeight(source, lineNumber, fields[0].strip());
        String language = fields[1].strip().toLowerCase(Locale.ROOT);
        if (!LANGUAGES.contains(language)) {
            throw error(source, lineNumber, "unknown language '" + language + "', expected one of " + LANGUAGES);
        }
        String phrase = fields[2].strip();
        if (phrase.isEmpty()) {
            throw error(source, lineNumber, "phrase is empty");
        }
        return new KeywordEntry(phrase, weight, language);
    }

    private static Weight parseWeight(String source, int lineNumber, String value) {
        try {
            return Weight.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw error(source, lineNumber, "unknown weight '" + value + "'");
        }
    }

    private static RuleDataException error(String source, int lineNumber, String message) {
        return new RuleDataException(source + ":" + lineNumber + ": " + message);
    }
}

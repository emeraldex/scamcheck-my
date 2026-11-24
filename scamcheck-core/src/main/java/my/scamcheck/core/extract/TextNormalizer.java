package my.scamcheck.core.extract;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Normalises text so that matching ignores case, spacing and look-alike Unicode
 * forms (SRS US-03 AC5). Used for both user input and keyword phrases, so the
 * two are always compared in the same form.
 */
public final class TextNormalizer {

    /** Any run of whitespace or Unicode space separators, including no-break spaces. */
    private static final Pattern WHITESPACE = Pattern.compile("[\\s\\p{Z}]+");

    /** Curly single quotes that phones insert automatically ("it’s"). */
    private static final Pattern CURLY_APOSTROPHE = Pattern.compile("[‘’]");

    private TextNormalizer() {
    }

    /**
     * NFKC-normalises (e.g. full-width "ＳＥＧＥＲＡ" becomes "SEGERA"), turns curly
     * apostrophes into straight ones, lower-cases with {@link Locale#ROOT},
     * collapses whitespace to single spaces, and trims.
     */
    public static String normalize(String text) {
        Objects.requireNonNull(text, "text");
        String compatible = Normalizer.normalize(text, Normalizer.Form.NFKC);
        String straightQuotes = CURLY_APOSTROPHE.matcher(compatible).replaceAll("'");
        String lowerCase = straightQuotes.toLowerCase(Locale.ROOT);
        return WHITESPACE.matcher(lowerCase).replaceAll(" ").strip();
    }
}

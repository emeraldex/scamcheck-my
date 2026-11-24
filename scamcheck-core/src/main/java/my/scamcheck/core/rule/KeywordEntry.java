package my.scamcheck.core.rule;

import java.util.Objects;
import my.scamcheck.core.extract.TextNormalizer;
import my.scamcheck.core.model.Weight;

/**
 * One phrase from a keyword data file (ADR-007).
 *
 * @param phrase   the phrase, stored normalised so it compares directly with normalised input
 * @param weight   how strong a sign this phrase is
 * @param language {@code en} or {@code ms}; documentation only, since every phrase is
 *                 matched whatever the UI language (ADR-009)
 */
public record KeywordEntry(String phrase, Weight weight, String language) {

    public KeywordEntry {
        Objects.requireNonNull(phrase, "phrase");
        Objects.requireNonNull(weight, "weight");
        Objects.requireNonNull(language, "language");
        phrase = TextNormalizer.normalize(phrase);
        if (phrase.isEmpty()) {
            throw new IllegalArgumentException("phrase must not be blank");
        }
    }
}

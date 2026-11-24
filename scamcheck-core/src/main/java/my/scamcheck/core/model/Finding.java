package my.scamcheck.core.model;

import java.util.List;
import java.util.Objects;

/**
 * One warning sign produced by a rule.
 *
 * @param ruleId      stable rule ID, e.g. {@code keyword.urgency}
 * @param category    scoring group
 * @param weight      strength of the sign
 * @param matchedText the part of the (normalised) input that triggered the rule
 * @param messageKey  key the adapter resolves to localised text (ADR-009);
 *                    adapters append {@code .title} and {@code .explanation}
 * @param messageArgs arguments for the localised message, may be empty
 */
public record Finding(
        String ruleId,
        Category category,
        Weight weight,
        String matchedText,
        String messageKey,
        List<String> messageArgs) {

    public Finding {
        Objects.requireNonNull(ruleId, "ruleId");
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(weight, "weight");
        Objects.requireNonNull(matchedText, "matchedText");
        Objects.requireNonNull(messageKey, "messageKey");
        messageArgs = List.copyOf(messageArgs);
    }

    /** Matched text is part of the user's input, so it is left out (ADR-008). */
    @Override
    public String toString() {
        return "Finding[ruleId=" + ruleId + ", category=" + category + ", weight=" + weight + "]";
    }
}

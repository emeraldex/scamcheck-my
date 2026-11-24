package my.scamcheck.core.rule.phone;

import java.util.List;
import my.scamcheck.core.model.Category;
import my.scamcheck.core.model.ExtractedInput;
import my.scamcheck.core.model.Finding;
import my.scamcheck.core.model.Weight;
import my.scamcheck.core.rule.Rule;

/**
 * Flags phone numbers whose format is one of the given kinds (SRS US-05).
 * Two instances are registered: invalid Malaysian numbers and international
 * numbers. Valid Malaysian numbers get no warning (AC1).
 */
public final class PhoneFormatRule implements Rule {

    public static final String INVALID_MY = "phone.invalid-my";
    public static final String INTERNATIONAL = "phone.international";

    private final String id;
    private final Weight weight;
    private final PhoneClassifier.Format flaggedFormat;

    private PhoneFormatRule(String id, Weight weight, PhoneClassifier.Format flaggedFormat) {
        this.id = id;
        this.weight = weight;
        this.flaggedFormat = flaggedFormat;
    }

    /** Looks Malaysian but has the wrong length or prefix (AC2). */
    public static PhoneFormatRule invalidMalaysian() {
        return new PhoneFormatRule(INVALID_MY, Weight.MODERATE, PhoneClassifier.Format.MALAYSIAN_INVALID);
    }

    /** A non-Malaysian country code (AC3). */
    public static PhoneFormatRule international() {
        return new PhoneFormatRule(INTERNATIONAL, Weight.WEAK, PhoneClassifier.Format.INTERNATIONAL);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Category category() {
        return Category.PHONE;
    }

    @Override
    public List<Finding> evaluate(ExtractedInput input) {
        return input.phones().stream()
                .filter(phone -> PhoneClassifier.classify(phone.digits()) == flaggedFormat)
                .map(phone -> new Finding(id, Category.PHONE, weight, phone.text(), "finding." + id, List.of()))
                .toList();
    }

    @Override
    public String toString() {
        return "PhoneFormatRule[id=" + id + "]";
    }
}

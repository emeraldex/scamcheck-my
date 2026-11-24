package my.scamcheck.core.model;

import java.util.Objects;

/**
 * A phone number found in the input.
 *
 * @param text   the number as written, e.g. {@code +60 12-345 6789}
 * @param digits the digits only, keeping a leading {@code +} if present, e.g. {@code +60123456789}
 */
public record PhoneCandidate(String text, String digits) {

    public PhoneCandidate {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(digits, "digits");
    }

    /** The number is user input, so it is left out (ADR-008). */
    @Override
    public String toString() {
        return "PhoneCandidate[digits=" + digits.length() + "]";
    }
}

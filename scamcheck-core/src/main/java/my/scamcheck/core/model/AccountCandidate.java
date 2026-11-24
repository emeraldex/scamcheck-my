package my.scamcheck.core.model;

import java.util.Objects;

/**
 * A digit sequence that looks like a bank account number (ADR-007 Q3: one
 * generic pattern of 10-16 digits, not already claimed as a phone number).
 *
 * @param text   the number as written, e.g. {@code 5123 4567 8901}
 * @param digits the digits only
 */
public record AccountCandidate(String text, String digits) {

    public AccountCandidate {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(digits, "digits");
    }

    /** The number is user input, so it is left out (ADR-008). */
    @Override
    public String toString() {
        return "AccountCandidate[digits=" + digits.length() + "]";
    }
}

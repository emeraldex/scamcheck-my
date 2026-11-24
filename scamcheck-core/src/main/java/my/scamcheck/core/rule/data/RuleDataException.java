package my.scamcheck.core.rule.data;

/** Thrown when a rule data file is missing or malformed. Fails fast at startup. */
public class RuleDataException extends RuntimeException {

    public RuleDataException(String message) {
        super(message);
    }

    public RuleDataException(String message, Throwable cause) {
        super(message, cause);
    }
}

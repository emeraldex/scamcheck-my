package my.scamcheck.api.error;

/**
 * A request field failed validation. Carries the field name and a safe message,
 * never the submitted value, so logging this exception can't leak input (ADR-008).
 */
public class InvalidRequestException extends RuntimeException {

    private final String field;

    public InvalidRequestException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String field() {
        return field;
    }
}

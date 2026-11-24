package my.scamcheck.core.rule;

import java.util.List;
import my.scamcheck.core.model.Category;
import my.scamcheck.core.model.ExtractedInput;
import my.scamcheck.core.model.Finding;

/**
 * One self-contained check (ADR-002).
 *
 * <p>Implementations must be stateless, thread-safe and free of side effects:
 * no I/O and no logging of input (ADR-008).
 */
public interface Rule {

    /** Stable ID used in the API and in message keys, e.g. {@code keyword.urgency}. */
    String id();

    /** Category used by the scorer to group findings. */
    Category category();

    /** Returns zero or more findings, never {@code null}. */
    List<Finding> evaluate(ExtractedInput input);
}

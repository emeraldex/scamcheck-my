package my.scamcheck.core.rule.link;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import my.scamcheck.core.model.Category;
import my.scamcheck.core.model.ExtractedInput;
import my.scamcheck.core.model.Finding;
import my.scamcheck.core.model.UrlCandidate;
import my.scamcheck.core.model.Weight;
import my.scamcheck.core.rule.Rule;

/**
 * Template for rules that look at one link at a time (SRS US-04). Subclasses
 * decide whether a link is suspicious; this class runs them over every extracted
 * link and reports each matched text once.
 */
abstract class LinkRule implements Rule {

    private final String id;
    private final Weight weight;

    LinkRule(String id, Weight weight) {
        this.id = id;
        this.weight = weight;
    }

    @Override
    public final String id() {
        return id;
    }

    @Override
    public final Category category() {
        return Category.LINK;
    }

    @Override
    public final List<Finding> evaluate(ExtractedInput input) {
        Set<String> reported = new HashSet<>();
        return input.urls().stream()
                .map(this::check)
                .flatMap(Optional::stream)
                .filter(finding -> reported.add(finding.matchedText()))
                .toList();
    }

    /** Returns a finding if this link is suspicious under this rule. */
    protected abstract Optional<Finding> check(UrlCandidate url);

    protected final Finding finding(String matchedText, String... messageArgs) {
        return new Finding(id, Category.LINK, weight, matchedText, "finding." + id, List.of(messageArgs));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[id=" + id + "]";
    }
}

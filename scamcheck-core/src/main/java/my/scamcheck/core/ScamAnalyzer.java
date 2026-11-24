package my.scamcheck.core;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import my.scamcheck.core.extract.InputExtractor;
import my.scamcheck.core.model.AnalysisInput;
import my.scamcheck.core.model.ExtractedInput;
import my.scamcheck.core.model.Finding;
import my.scamcheck.core.model.RiskAssessment;
import my.scamcheck.core.rule.Rule;
import my.scamcheck.core.score.RiskScorer;

/**
 * The engine's single entry point (ADR-002): extract once, run every rule,
 * score the findings. Stateless and thread-safe if its rules are.
 *
 * <p>Collaborators come in through the constructor, so adapters wire it however
 * they like and tests can pass hand-made rules.
 */
public class ScamAnalyzer {

    private final InputExtractor extractor;
    private final List<Rule> rules;
    private final RiskScorer scorer;

    public ScamAnalyzer(InputExtractor extractor, List<Rule> rules, RiskScorer scorer) {
        this.extractor = Objects.requireNonNull(extractor, "extractor");
        this.scorer = Objects.requireNonNull(scorer, "scorer");
        this.rules = List.copyOf(rules);
        if (this.rules.isEmpty()) {
            throw new IllegalArgumentException("at least one rule is required");
        }
        Set<String> ids = new HashSet<>();
        for (Rule rule : this.rules) {
            if (!ids.add(rule.id())) {
                throw new IllegalArgumentException("duplicate rule id: " + rule.id());
            }
        }
    }

    public RiskAssessment analyze(AnalysisInput input) {
        ExtractedInput extracted = extractor.extract(input);
        List<Finding> findings = rules.stream()
                .flatMap(rule -> rule.evaluate(extracted).stream())
                .toList();
        return scorer.score(findings);
    }

    /** The registered rules, e.g. for checking every rule has localised messages (ADR-009). */
    public List<Rule> rules() {
        return rules;
    }
}

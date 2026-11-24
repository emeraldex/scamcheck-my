package my.scamcheck.core.rule.link;

import java.util.Optional;
import java.util.Set;
import my.scamcheck.core.extract.DomainNames;
import my.scamcheck.core.model.Finding;
import my.scamcheck.core.model.UrlCandidate;
import my.scamcheck.core.model.Weight;

/**
 * Flags domain endings often used for short-lived scam sites (SRS US-04 AC4).
 * Message argument {@code {0}} is the ending, e.g. {@code .xyz}.
 */
public final class UnusualTldRule extends LinkRule {

    public static final String ID = "link.unusual-tld";

    private final Set<String> unusualTlds;

    public UnusualTldRule(Set<String> unusualTlds) {
        super(ID, Weight.WEAK);
        this.unusualTlds = Set.copyOf(unusualTlds);
    }

    @Override
    protected Optional<Finding> check(UrlCandidate url) {
        if (url.ipAddress()) {
            return Optional.empty();
        }
        String tld = DomainNames.lastLabel(url.host());
        return unusualTlds.contains(tld) ? Optional.of(finding(url.host(), "." + tld)) : Optional.empty();
    }
}

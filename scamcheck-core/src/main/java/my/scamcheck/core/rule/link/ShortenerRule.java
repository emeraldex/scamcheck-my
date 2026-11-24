package my.scamcheck.core.rule.link;

import java.util.Optional;
import java.util.Set;
import my.scamcheck.core.extract.DomainNames;
import my.scamcheck.core.model.Finding;
import my.scamcheck.core.model.UrlCandidate;
import my.scamcheck.core.model.Weight;

/** Flags URL shorteners, which hide the real destination (SRS US-04 AC3). */
public final class ShortenerRule extends LinkRule {

    public static final String ID = "link.shortener";

    private final Set<String> shortenerDomains;

    public ShortenerRule(Set<String> shortenerDomains) {
        super(ID, Weight.MODERATE);
        this.shortenerDomains = Set.copyOf(shortenerDomains);
    }

    @Override
    protected Optional<Finding> check(UrlCandidate url) {
        boolean shortener = shortenerDomains.contains(url.host())
                || shortenerDomains.contains(DomainNames.registrableDomain(url.host()));
        return shortener ? Optional.of(finding(url.host())) : Optional.empty();
    }
}

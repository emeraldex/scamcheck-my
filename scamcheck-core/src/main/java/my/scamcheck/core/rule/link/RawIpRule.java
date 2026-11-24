package my.scamcheck.core.rule.link;

import java.util.Optional;
import my.scamcheck.core.model.Finding;
import my.scamcheck.core.model.UrlCandidate;
import my.scamcheck.core.model.Weight;

/** Flags links that use a raw IP address instead of a name (SRS US-04 AC5). */
public final class RawIpRule extends LinkRule {

    public static final String ID = "link.raw-ip";

    public RawIpRule() {
        super(ID, Weight.MODERATE);
    }

    @Override
    protected Optional<Finding> check(UrlCandidate url) {
        return url.ipAddress() ? Optional.of(finding(url.host())) : Optional.empty();
    }
}

package my.scamcheck.core.rule.link;

import java.util.Optional;
import my.scamcheck.core.model.Finding;
import my.scamcheck.core.model.UrlCandidate;
import my.scamcheck.core.model.Weight;

/**
 * Flags plain {@code http://} links (SRS US-04 AC6). Links written without a
 * scheme are not flagged: browsers usually upgrade them to https.
 */
public final class NotHttpsRule extends LinkRule {

    public static final String ID = "link.not-https";

    public NotHttpsRule() {
        super(ID, Weight.WEAK);
    }

    @Override
    protected Optional<Finding> check(UrlCandidate url) {
        return "http".equals(url.scheme()) ? Optional.of(finding("http://" + url.host())) : Optional.empty();
    }
}

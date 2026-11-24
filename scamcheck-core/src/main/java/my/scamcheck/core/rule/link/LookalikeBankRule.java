package my.scamcheck.core.rule.link;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import my.scamcheck.core.extract.DomainNames;
import my.scamcheck.core.model.Finding;
import my.scamcheck.core.model.UrlCandidate;
import my.scamcheck.core.model.Weight;
import my.scamcheck.core.rule.data.BankDomains;

/**
 * Flags links that look like a Malaysian bank but aren't one of its official
 * domains (SRS US-04 AC1-AC2). A link is a lookalike when its domain is not
 * official and either:
 * <ul>
 *   <li>a part of its host (split on "." and "-") starts or ends with a bank's brand
 *       token, e.g. {@code maybank2u-secure.xyz}; or</li>
 *   <li>its registrable domain is one character away from an official one,
 *       e.g. {@code maybamk2u.com.my}.</li>
 * </ul>
 * Message argument {@code {0}} is the bank's display name.
 */
public final class LookalikeBankRule extends LinkRule {

    public static final String ID = "link.lookalike-bank";

    private final List<BankDomains> banks;

    public LookalikeBankRule(List<BankDomains> banks) {
        super(ID, Weight.STRONG);
        this.banks = List.copyOf(banks);
    }

    @Override
    protected Optional<Finding> check(UrlCandidate url) {
        if (url.ipAddress()) {
            return Optional.empty();
        }
        String domain = DomainNames.registrableDomain(url.host());
        if (isOfficial(domain)) {
            return Optional.empty();
        }
        List<String> hostParts = Arrays.asList(url.host().split("[.-]"));
        return banks.stream()
                .filter(bank -> usesBrandToken(bank, hostParts) || isOneEditFromOfficial(bank, domain))
                .findFirst()
                .map(bank -> finding(url.host(), bank.displayName()));
    }

    private boolean isOfficial(String domain) {
        return banks.stream().anyMatch(bank -> bank.officialDomains().contains(domain));
    }

    private static boolean usesBrandToken(BankDomains bank, List<String> hostParts) {
        return hostParts.stream().anyMatch(part ->
                bank.brandTokens().stream().anyMatch(token -> part.startsWith(token) || part.endsWith(token)));
    }

    private static boolean isOneEditFromOfficial(BankDomains bank, String domain) {
        return bank.officialDomains().stream().anyMatch(official -> EditDistance.isExactlyOne(domain, official));
    }
}

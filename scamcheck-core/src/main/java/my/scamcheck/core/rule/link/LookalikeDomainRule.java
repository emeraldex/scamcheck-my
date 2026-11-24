package my.scamcheck.core.rule.link;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import my.scamcheck.core.extract.DomainNames;
import my.scamcheck.core.model.Finding;
import my.scamcheck.core.model.UrlCandidate;
import my.scamcheck.core.model.Weight;
import my.scamcheck.core.rule.data.BrandDomains;

/**
 * Flags links that look like a known brand but aren't on one of its official
 * domains (SRS US-04 AC1-AC2). Two instances run: one for banks, one for other
 * brands people are commonly phished with (couriers, e-wallets, marketplaces,
 * telcos and government sites).
 *
 * <p>A link is a lookalike when its host isn't on an official domain (or a
 * subdomain of one) and either:
 * <ul>
 *   <li>a part of its host (split on "." and "-") starts or ends with a brand
 *       token, e.g. {@code maybank2u-secure.xyz} or {@code poslaju-semak.top}; or</li>
 *   <li>its registrable domain is one character away from an official one,
 *       e.g. {@code maybamk2u.com.my}.</li>
 * </ul>
 * Message argument {@code {0}} is the brand's display name.
 */
public final class LookalikeDomainRule extends LinkRule {

    public static final String BANK_ID = "link.lookalike-bank";
    public static final String BRAND_ID = "link.lookalike-brand";

    private final List<BrandDomains> brands;

    public LookalikeDomainRule(String id, List<BrandDomains> brands) {
        super(id, Weight.STRONG);
        this.brands = List.copyOf(brands);
    }

    /** The bank lookalike rule. */
    public static LookalikeDomainRule banks(List<BrandDomains> banks) {
        return new LookalikeDomainRule(BANK_ID, banks);
    }

    /** The lookalike rule for non-bank brands. */
    public static LookalikeDomainRule brands(List<BrandDomains> brands) {
        return new LookalikeDomainRule(BRAND_ID, brands);
    }

    @Override
    protected Optional<Finding> check(UrlCandidate url) {
        if (url.ipAddress() || isOfficial(url.host())) {
            return Optional.empty();
        }
        String domain = DomainNames.registrableDomain(url.host());
        List<String> hostParts = Arrays.asList(url.host().split("[.-]"));
        return brands.stream()
                .filter(brand -> usesBrandToken(brand, hostParts) || isOneEditFromOfficial(brand, domain))
                .findFirst()
                .map(brand -> finding(url.host(), brand.displayName()));
    }

    /** The host is an official domain or a subdomain of one, e.g. {@code www.hasil.gov.my} under {@code gov.my}. */
    private boolean isOfficial(String host) {
        return brands.stream()
                .flatMap(brand -> brand.officialDomains().stream())
                .anyMatch(official -> host.equals(official) || host.endsWith("." + official));
    }

    private static boolean usesBrandToken(BrandDomains brand, List<String> hostParts) {
        return hostParts.stream().anyMatch(part ->
                brand.brandTokens().stream().anyMatch(token -> part.startsWith(token) || part.endsWith(token)));
    }

    private static boolean isOneEditFromOfficial(BrandDomains brand, String domain) {
        return brand.officialDomains().stream().anyMatch(official -> EditDistance.isExactlyOne(domain, official));
    }
}

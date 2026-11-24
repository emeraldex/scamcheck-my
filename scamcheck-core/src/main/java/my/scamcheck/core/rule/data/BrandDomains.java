package my.scamcheck.core.rule.data;

import java.util.Objects;
import java.util.Set;

/**
 * One brand from {@code bank-domains.txt} or {@code brand-domains.txt} (ADR-007).
 *
 * @param displayName     name shown to the user, e.g. "Maybank" or "Pos Laju"
 * @param brandTokens     lower-case tokens that suggest the brand, e.g. "maybank2u"
 * @param officialDomains the brand's official domains; subdomains of these are official too
 */
public record BrandDomains(String displayName, Set<String> brandTokens, Set<String> officialDomains) {

    public BrandDomains {
        Objects.requireNonNull(displayName, "displayName");
        brandTokens = Set.copyOf(brandTokens);
        officialDomains = Set.copyOf(officialDomains);
        if (brandTokens.isEmpty() || officialDomains.isEmpty()) {
            throw new IllegalArgumentException(displayName + " needs at least one token and one official domain");
        }
    }
}

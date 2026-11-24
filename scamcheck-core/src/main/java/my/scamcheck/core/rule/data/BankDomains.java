package my.scamcheck.core.rule.data;

import java.util.Objects;
import java.util.Set;

/**
 * One bank from {@code bank-domains.txt} (ADR-007).
 *
 * @param displayName     name shown to the user, e.g. "Maybank"
 * @param brandTokens     lower-case tokens that suggest the bank, e.g. "maybank2u"
 * @param officialDomains the bank's official registrable domains
 */
public record BankDomains(String displayName, Set<String> brandTokens, Set<String> officialDomains) {

    public BankDomains {
        Objects.requireNonNull(displayName, "displayName");
        brandTokens = Set.copyOf(brandTokens);
        officialDomains = Set.copyOf(officialDomains);
        if (brandTokens.isEmpty() || officialDomains.isEmpty()) {
            throw new IllegalArgumentException(displayName + " needs at least one token and one official domain");
        }
    }
}

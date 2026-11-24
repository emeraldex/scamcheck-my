package my.scamcheck.core.rule.link;

import java.util.List;
import my.scamcheck.core.rule.Rule;
import my.scamcheck.core.rule.data.BankDomainsLoader;
import my.scamcheck.core.rule.data.SimpleListLoader;

/** Builds the five v1 link rules from their data files (ARCHITECTURE.md §4.2). */
public final class LinkRules {

    private LinkRules() {
    }

    public static List<Rule> defaults() {
        return List.of(
                new LookalikeBankRule(BankDomainsLoader.loadResource("/rules/bank-domains.txt")),
                new ShortenerRule(SimpleListLoader.loadResource("/rules/url-shorteners.txt")),
                new UnusualTldRule(SimpleListLoader.loadResource("/rules/unusual-tlds.txt")),
                new RawIpRule(),
                new NotHttpsRule());
    }
}

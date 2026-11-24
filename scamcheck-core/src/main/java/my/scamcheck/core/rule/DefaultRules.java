package my.scamcheck.core.rule;

import java.util.ArrayList;
import java.util.List;
import my.scamcheck.core.rule.account.AccountNumberRule;
import my.scamcheck.core.rule.link.LinkRules;
import my.scamcheck.core.rule.phone.PhoneFormatRule;

/** Every v1 rule, in one list. Adapters register this with {@code ScamAnalyzer}. */
public final class DefaultRules {

    private DefaultRules() {
    }

    public static List<Rule> all() {
        List<Rule> rules = new ArrayList<>(KeywordRules.defaults());
        rules.addAll(LinkRules.defaults());
        rules.add(PhoneFormatRule.invalidMalaysian());
        rules.add(PhoneFormatRule.international());
        rules.add(new AccountNumberRule());
        return List.copyOf(rules);
    }
}

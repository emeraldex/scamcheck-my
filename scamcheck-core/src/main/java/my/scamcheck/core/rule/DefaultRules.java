package my.scamcheck.core.rule;

import java.util.ArrayList;
import java.util.List;
import my.scamcheck.core.rule.link.LinkRules;

/** Every v1 rule, in one list. Adapters register this with {@code ScamAnalyzer}. */
public final class DefaultRules {

    private DefaultRules() {
    }

    public static List<Rule> all() {
        List<Rule> rules = new ArrayList<>(KeywordRules.defaults());
        rules.addAll(LinkRules.defaults());
        return List.copyOf(rules);
    }
}

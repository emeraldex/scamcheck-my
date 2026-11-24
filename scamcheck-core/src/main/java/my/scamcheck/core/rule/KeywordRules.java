package my.scamcheck.core.rule;

import java.util.List;
import my.scamcheck.core.model.Category;
import my.scamcheck.core.rule.data.KeywordListLoader;

/** Builds the v1 keyword rules from their data files (ARCHITECTURE.md §4.2). */
public final class KeywordRules {

    private static final String BASE = "/rules/keywords/";

    private KeywordRules() {
    }

    public static List<Rule> defaults() {
        return List.of(
                fromFile("keyword.investment", Category.INVESTMENT, "investment.txt"),
                fromFile("keyword.urgency", Category.URGENCY, "urgency.txt"),
                fromFile("keyword.account-threat", Category.ACCOUNT_THREAT, "account-threat.txt"),
                fromFile("keyword.impersonation", Category.IMPERSONATION, "impersonation.txt"),
                fromFile("keyword.credential-request", Category.CREDENTIAL_REQUEST, "credential-request.txt"),
                fromFile("keyword.prize", Category.PRIZE, "prize.txt"),
                fromFile("keyword.job-offer", Category.JOB_OFFER, "job-offer.txt"),
                fromFile("keyword.money-request", Category.MONEY_REQUEST, "money-request.txt"));
    }

    private static Rule fromFile(String id, Category category, String fileName) {
        return new KeywordRule(id, category, KeywordListLoader.loadResource(BASE + fileName));
    }
}

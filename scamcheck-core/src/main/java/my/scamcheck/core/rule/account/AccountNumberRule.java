package my.scamcheck.core.rule.account;

import java.util.List;
import my.scamcheck.core.model.Category;
import my.scamcheck.core.model.ExtractedInput;
import my.scamcheck.core.model.Finding;
import my.scamcheck.core.model.Weight;
import my.scamcheck.core.rule.Rule;

/**
 * Points out possible bank account numbers so the user checks them on Semak Mule
 * before transferring (SRS US-06). The app can't tell whether an account is a
 * mule account (AC2), so the weight is INFO and an account alone never rates
 * above Medium (AC4, ADR-005).
 */
public final class AccountNumberRule implements Rule {

    public static final String ID = "account.detected";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Category category() {
        return Category.ACCOUNT;
    }

    @Override
    public List<Finding> evaluate(ExtractedInput input) {
        return input.accounts().stream()
                .map(account -> new Finding(ID, Category.ACCOUNT, Weight.INFO, account.text(), "finding." + ID, List.of()))
                .toList();
    }

    @Override
    public String toString() {
        return "AccountNumberRule[id=" + ID + "]";
    }
}

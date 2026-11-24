package my.scamcheck.core.extract;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import my.scamcheck.core.model.AccountCandidate;
import my.scamcheck.core.model.PhoneCandidate;

/**
 * Finds phone numbers and bank account numbers (SRS US-05, US-06), and decides
 * which is which so the same digits are never reported as both (US-06 AC3).
 *
 * <p>A digit run (8-20 digits, optionally grouped with single spaces or hyphens)
 * is a <b>phone number</b> when it:
 * <ul>
 *   <li>starts with {@code +} (any country), or with {@code 00} and has 10-15 digits, or</li>
 *   <li>starts with {@code 0} and has 8-12 digits (Malaysian national format), or</li>
 *   <li>starts with {@code 601} and has 11-12 digits (+60 mobile written without the plus), or</li>
 *   <li>starts with {@code 1300} or {@code 1800} and has 10 digits.</li>
 * </ul>
 * Otherwise it is an <b>account number</b> when it has 10-16 digits. Anything else
 * (dates, short codes, amounts) is ignored. Digits inside links are skipped.
 */
public class NumberExtractor {

    /** Not preceded by a letter, digit or characters that mean "inside a link, email or amount". */
    private static final Pattern DIGIT_RUN = Pattern.compile(
            "(?<![\\p{L}\\p{N}+./:@=&#_,-])(\\+?\\d(?:[ -]?\\d){7,19})(?![\\p{L}\\p{N}])");

    private static final Pattern NON_DIGITS = Pattern.compile("\\D");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    public record Result(List<PhoneCandidate> phones, List<AccountCandidate> accounts) {
        public Result {
            phones = List.copyOf(phones);
            accounts = List.copyOf(accounts);
        }
    }

    public Result extract(String text) {
        Objects.requireNonNull(text, "text");
        Map<String, PhoneCandidate> phones = new LinkedHashMap<>();
        Map<String, AccountCandidate> accounts = new LinkedHashMap<>();

        Matcher matcher = DIGIT_RUN.matcher(text);
        while (matcher.find()) {
            for (String part : splitIfTooLong(matcher.group(1))) {
                classify(part, phones, accounts);
            }
        }
        return new Result(new ArrayList<>(phones.values()), new ArrayList<>(accounts.values()));
    }

    /** Two numbers separated by a space can merge into one run; split them again if it's too long for either. */
    private static List<String> splitIfTooLong(String run) {
        if (digitsOf(run).length() <= 16) {
            return List.of(run);
        }
        return List.of(WHITESPACE.split(run));
    }

    private static void classify(String text, Map<String, PhoneCandidate> phones, Map<String, AccountCandidate> accounts) {
        String digits = digitsOf(text);
        boolean plus = text.startsWith("+");
        int length = digits.length();

        if (plus && length >= 8) {
            phones.putIfAbsent("+" + digits, new PhoneCandidate(text, "+" + digits));
        } else if (isPhoneLike(digits, length)) {
            phones.putIfAbsent(digits, new PhoneCandidate(text, digits));
        } else if (length >= 10 && length <= 16) {
            accounts.putIfAbsent(digits, new AccountCandidate(text, digits));
        }
    }

    private static boolean isPhoneLike(String digits, int length) {
        return (digits.startsWith("00") && length >= 10 && length <= 15) // 00 international dialling prefix
                || (digits.startsWith("0") && length >= 8 && length <= 12)
                || (digits.startsWith("601") && (length == 11 || length == 12))
                || ((digits.startsWith("1300") || digits.startsWith("1800")) && length == 10);
    }

    private static String digitsOf(String text) {
        return NON_DIGITS.matcher(text).replaceAll("");
    }
}

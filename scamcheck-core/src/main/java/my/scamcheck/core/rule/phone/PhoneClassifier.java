package my.scamcheck.core.rule.phone;

/**
 * Classifies a phone number's format (SRS US-05). Format only: it can't tell
 * whether a number belongs to a scammer, which is what Semak Mule is for.
 *
 * <p>Malaysian formats treated as valid (national form, with the leading 0):
 * <ul>
 *   <li>Mobile {@code 010, 012-014, 016-019}: 10 digits; {@code 011}: 11 digits;
 *       {@code 015}: 10 or 11 digits</li>
 *   <li>Landline {@code 03}: 10 digits; {@code 04-07, 09}: 9 digits; {@code 082-089}: 9 digits</li>
 *   <li>Toll-free and local-rate {@code 1300}, {@code 1800}: 10 digits</li>
 * </ul>
 * Numbers in {@code +60} or {@code 60} form are converted to national form first.
 * {@code +} or {@code 00} followed by another country code is international.
 */
final class PhoneClassifier {

    enum Format { MALAYSIAN_VALID, MALAYSIAN_INVALID, INTERNATIONAL }

    private PhoneClassifier() {
    }

    /** @param digits digits only, with a leading {@code +} if it was written */
    static Format classify(String digits) {
        String national;
        if (digits.startsWith("+60")) {
            national = "0" + digits.substring(3);
        } else if (digits.startsWith("+") || digits.startsWith("00")) {
            return digits.startsWith("0060") ? classifyNational("0" + digits.substring(4)) : Format.INTERNATIONAL;
        } else if (digits.startsWith("60")) {
            national = "0" + digits.substring(2);
        } else {
            national = digits;
        }
        return classifyNational(national);
    }

    private static Format classifyNational(String n) {
        return isValidNational(n) ? Format.MALAYSIAN_VALID : Format.MALAYSIAN_INVALID;
    }

    private static boolean isValidNational(String n) {
        int length = n.length();
        if (n.startsWith("1300") || n.startsWith("1800")) {
            return length == 10;
        }
        if (n.startsWith("011")) {
            return length == 11;
        }
        if (n.startsWith("015")) {
            return length == 10 || length == 11;
        }
        if (n.matches("01[02346789].*")) {
            return length == 10;
        }
        if (n.startsWith("03")) {
            return length == 10;
        }
        if (n.matches("0[4-79].*") || n.matches("08[2-9].*")) {
            return length == 9;
        }
        return false;
    }
}

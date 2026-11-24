package my.scamcheck.core.rule.link;

/** Checks whether two strings differ by exactly one insert, delete or substitution. */
final class EditDistance {

    private EditDistance() {
    }

    static boolean isExactlyOne(String a, String b) {
        if (a.equals(b) || Math.abs(a.length() - b.length()) > 1) {
            return false;
        }
        String shorter = a.length() <= b.length() ? a : b;
        String longer = a.length() <= b.length() ? b : a;
        boolean sameLength = shorter.length() == longer.length();

        int i = 0;
        while (i < shorter.length() && shorter.charAt(i) == longer.charAt(i)) {
            i++;
        }
        // After the first difference, the rest must match once one character is
        // skipped: in both strings (substitution) or only in the longer (insert/delete).
        String restOfShorter = sameLength ? shorter.substring(i + 1) : shorter.substring(i);
        return longer.substring(i + 1).equals(restOfShorter);
    }
}

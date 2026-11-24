package my.scamcheck.core.extract;

import java.util.Set;

/**
 * Small domain-name helpers (ADR-007).
 *
 * <p>The registrable domain is the last two labels, or the last three under a
 * Malaysian second-level suffix such as {@code com.my}. This is deliberately not
 * the full Public Suffix List: good enough for Malaysian bank domains, and
 * documented as a known limitation.
 */
public final class DomainNames {

    private static final Set<String> MY_SECOND_LEVEL = Set.of(
            "com.my", "net.my", "org.my", "gov.my", "edu.my", "mil.my", "name.my");

    private DomainNames() {
    }

    /** {@code www.maybank2u.com.my} → {@code maybank2u.com.my}; {@code a.b.example.com} → {@code example.com}. */
    public static String registrableDomain(String host) {
        String[] labels = host.split("\\.");
        if (labels.length <= 2) {
            return host;
        }
        String lastTwo = labels[labels.length - 2] + "." + labels[labels.length - 1];
        int keep = MY_SECOND_LEVEL.contains(lastTwo) ? 3 : 2;
        return String.join(".", java.util.Arrays.copyOfRange(labels, labels.length - keep, labels.length));
    }

    /** The top-level domain: {@code maybank2u-secure.xyz} → {@code xyz}. */
    public static String lastLabel(String host) {
        int dot = host.lastIndexOf('.');
        return dot >= 0 ? host.substring(dot + 1) : host;
    }
}

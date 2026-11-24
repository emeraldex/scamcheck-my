package my.scamcheck.core.rule.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Reads {@code bank-domains.txt} and {@code brand-domains.txt}: {@code Display name | token, token | domain, domain}.
 * Fails fast on malformed lines, a domain listed twice, or an empty file.
 */
public final class BrandDomainsLoader {

    private BrandDomainsLoader() {
    }

    public static List<BrandDomains> loadResource(String resourcePath) {
        return parse(resourcePath, SimpleListLoader.readLines(resourcePath));
    }

    public static List<BrandDomains> parse(String source, List<String> lines) {
        List<BrandDomains> brands = new ArrayList<>();
        Set<String> seenDomains = new HashSet<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).strip();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] fields = line.split("\\|", -1);
            if (fields.length != 3) {
                throw error(source, i + 1, "expected 'Display name | tokens | domains' but found " + fields.length + " field(s)");
            }
            String name = fields[0].strip();
            Set<String> tokens = commaSeparated(fields[1]);
            Set<String> domains = commaSeparated(fields[2]);
            if (name.isEmpty() || tokens.isEmpty() || domains.isEmpty()) {
                throw error(source, i + 1, "name, tokens and domains must all be present");
            }
            for (String domain : domains) {
                if (!seenDomains.add(domain)) {
                    throw error(source, i + 1, "domain '" + domain + "' is listed more than once");
                }
            }
            brands.add(new BrandDomains(name, tokens, domains));
        }

        if (brands.isEmpty()) {
            throw new RuleDataException(source + ": no entries found");
        }
        return List.copyOf(brands);
    }

    private static Set<String> commaSeparated(String field) {
        Set<String> values = new LinkedHashSet<>();
        Arrays.stream(field.split(","))
                .map(value -> value.strip().toLowerCase(Locale.ROOT))
                .filter(value -> !value.isEmpty())
                .forEach(values::add);
        return values;
    }

    private static RuleDataException error(String source, int lineNumber, String message) {
        return new RuleDataException(source + ":" + lineNumber + ": " + message);
    }
}

package my.scamcheck.core.extract;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import my.scamcheck.core.model.UrlCandidate;
import my.scamcheck.core.rule.data.SimpleListLoader;

/**
 * Finds links in free text (SRS US-04), with or without {@code http(s)://} (AC7).
 * It only parses text; it never opens or fetches a link (AC8).
 *
 * <p>Links written without a scheme are accepted only when they start with
 * {@code www.} or end in a recognised top-level domain, so ordinary chat like
 * "ok.jom" is not mistaken for a link. Raw IP addresses without a scheme need a
 * port or path ("192.0.2.1/login"), so version numbers like "1.2.3.4" aren't links.
 */
public class UrlExtractor {

    private static final Pattern WITH_SCHEME = Pattern.compile("(?i)\\bhttps?://[^\\s<>\"'`]+");

    private static final Pattern WITHOUT_SCHEME = Pattern.compile(
            "(?i)(?<![\\p{L}\\p{N}@./:_-])"
                    + "((?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z]{2,24}|\\d{1,3}(?:\\.\\d{1,3}){3})"
                    + "(:\\d{1,5})?"
                    + "(/[^\\s<>\"'`]*)?"
                    + "(?![\\p{L}\\p{N}@_-])");

    private static final Pattern IPV4 = Pattern.compile("\\d{1,3}(?:\\.\\d{1,3}){3}");
    private static final String TRAILING_PUNCTUATION = ".,;:!?)]}'\"";

    private final Set<String> recognisedTlds;

    public UrlExtractor(Set<String> recognisedTlds) {
        this.recognisedTlds = Set.copyOf(recognisedTlds);
    }

    /** Uses {@code /rules/tlds.txt} and {@code /rules/unusual-tlds.txt} as the recognised endings. */
    public static UrlExtractor withDefaultTlds() {
        var tlds = new HashSet<>(SimpleListLoader.loadResource("/rules/tlds.txt"));
        tlds.addAll(SimpleListLoader.loadResource("/rules/unusual-tlds.txt"));
        return new UrlExtractor(tlds);
    }

    public List<UrlCandidate> extract(String text) {
        Objects.requireNonNull(text, "text");
        TreeMap<Integer, UrlCandidate> byPosition = new TreeMap<>();
        List<int[]> schemeSpans = new ArrayList<>();

        Matcher withScheme = WITH_SCHEME.matcher(text);
        while (withScheme.find()) {
            schemeSpans.add(new int[] {withScheme.start(), withScheme.end()});
            int start = withScheme.start();
            parse(trimTrailingPunctuation(withScheme.group())).ifPresent(url -> byPosition.put(start, url));
        }

        Matcher withoutScheme = WITHOUT_SCHEME.matcher(text);
        while (withoutScheme.find()) {
            int start = withoutScheme.start();
            if (schemeSpans.stream().anyMatch(span -> start >= span[0] && start < span[1])) {
                continue;
            }
            String host = withoutScheme.group(1).toLowerCase(Locale.ROOT);
            boolean hasPortOrPath = withoutScheme.group(2) != null || withoutScheme.group(3) != null;
            if (isAcceptedWithoutScheme(host, hasPortOrPath)) {
                parse(trimTrailingPunctuation(withoutScheme.group())).ifPresent(url -> byPosition.put(start, url));
            }
        }

        // In order of appearance, first occurrence of each link only.
        Map<String, UrlCandidate> unique = new LinkedHashMap<>();
        byPosition.values().forEach(url -> unique.putIfAbsent(url.text(), url));
        return List.copyOf(unique.values());
    }

    private boolean isAcceptedWithoutScheme(String host, boolean hasPortOrPath) {
        if (IPV4.matcher(host).matches()) {
            return hasPortOrPath;
        }
        return host.startsWith("www.") || recognisedTlds.contains(DomainNames.lastLabel(host));
    }

    private static Optional<UrlCandidate> parse(String text) {
        String scheme = null;
        String rest = text;
        int schemeEnd = text.indexOf("://");
        if (schemeEnd > 0) {
            scheme = text.substring(0, schemeEnd).toLowerCase(Locale.ROOT);
            rest = text.substring(schemeEnd + 3);
        }

        String authority = rest.split("[/?#]", 2)[0];
        // "https://maybank2u.com.my@evil.example" really goes to evil.example.
        String hostAndPort = authority.substring(authority.lastIndexOf('@') + 1);
        String host = stripPort(hostAndPort).toLowerCase(Locale.ROOT);
        while (host.endsWith(".")) {
            host = host.substring(0, host.length() - 1);
        }
        if (host.isEmpty()) {
            return Optional.empty();
        }
        boolean ip = IPV4.matcher(host).matches() || host.startsWith("[");
        return Optional.of(new UrlCandidate(text, scheme, host, ip));
    }

    private static String stripPort(String hostAndPort) {
        if (hostAndPort.startsWith("[")) { // IPv6 literal, e.g. [2001:db8::1]:8080
            int close = hostAndPort.indexOf(']');
            return close > 0 ? hostAndPort.substring(0, close + 1) : hostAndPort;
        }
        int colon = hostAndPort.indexOf(':');
        return colon >= 0 ? hostAndPort.substring(0, colon) : hostAndPort;
    }

    private static String trimTrailingPunctuation(String text) {
        int end = text.length();
        while (end > 0 && TRAILING_PUNCTUATION.indexOf(text.charAt(end - 1)) >= 0) {
            end--;
        }
        return text.substring(0, end);
    }
}

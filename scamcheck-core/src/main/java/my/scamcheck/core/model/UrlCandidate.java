package my.scamcheck.core.model;

import java.util.Objects;

/**
 * A link found in the input.
 *
 * @param text      the link as written, minus trailing punctuation
 * @param scheme    {@code http} or {@code https}, or {@code null} when written without one
 * @param host      lower-case host name or IP address, without port or user info
 * @param ipAddress whether the host is a raw IP address
 */
public record UrlCandidate(String text, String scheme, String host, boolean ipAddress) {

    public UrlCandidate {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(host, "host");
    }

    /** The link is user input, so it is left out (ADR-008). */
    @Override
    public String toString() {
        return "UrlCandidate[scheme=" + scheme + ", ipAddress=" + ipAddress + "]";
    }
}

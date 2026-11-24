package my.scamcheck.core.model;

/**
 * Groups findings for scoring. Within one category only the strongest finding
 * counts towards the score (ADR-005).
 */
public enum Category {
    INVESTMENT,
    URGENCY,
    ACCOUNT_THREAT,
    IMPERSONATION,
    CREDENTIAL_REQUEST,
    LINK,
    PHONE,
    ACCOUNT
}

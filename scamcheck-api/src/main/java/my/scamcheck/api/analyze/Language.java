package my.scamcheck.api.analyze;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

/** Language for explanation text (SRS US-09 AC4). Written as {@code en} or {@code ms} in JSON. */
public enum Language {
    EN("en", Locale.ENGLISH),
    MS("ms", Locale.of("ms"));

    /** BM is the fallback language (SRS US-09 AC1). */
    public static final Language DEFAULT = MS;

    private final String code;
    private final Locale locale;

    Language(String code, Locale locale) {
        this.code = code;
        this.locale = locale;
    }

    @JsonValue
    public String code() {
        return code;
    }

    public Locale locale() {
        return locale;
    }

    @JsonCreator
    public static Language fromCode(String code) {
        for (Language language : values()) {
            if (language.code.equals(code)) {
                return language;
            }
        }
        throw new IllegalArgumentException("language must be 'en' or 'ms'");
    }
}

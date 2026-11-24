package my.scamcheck.core.rule.phone;

import static org.assertj.core.api.Assertions.assertThat;

import my.scamcheck.core.rule.phone.PhoneClassifier.Format;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PhoneClassifierTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            // Valid Malaysian (SRS US-05 AC1)
            "0123456789,     MALAYSIAN_VALID",
            "+60123456789,   MALAYSIAN_VALID",
            "60123456789,    MALAYSIAN_VALID",
            "01123456789,    MALAYSIAN_VALID",
            "0154567890,     MALAYSIAN_VALID",
            "01545678901,    MALAYSIAN_VALID",
            "0312345678,     MALAYSIAN_VALID",
            "041234567,      MALAYSIAN_VALID",
            "082123456,      MALAYSIAN_VALID",
            "1300881234,     MALAYSIAN_VALID",
            "1800881234,     MALAYSIAN_VALID",
            "0060123456789,  MALAYSIAN_VALID",
            // Looks Malaysian but invalid (AC2)
            "012345678,      MALAYSIAN_INVALID",
            "01234567890,    MALAYSIAN_INVALID",
            "0112345678,     MALAYSIAN_INVALID",
            "+601234567,     MALAYSIAN_INVALID",
            "0212345678,     MALAYSIAN_INVALID",
            "081123456,      MALAYSIAN_INVALID",
            "031234567,      MALAYSIAN_INVALID",
            // International (AC3)
            "+6561234567,    INTERNATIONAL",
            "+855123456789,  INTERNATIONAL",
            "0065612345678,  INTERNATIONAL"
    })
    void classifiesFormat(String digits, Format expected) {
        assertThat(PhoneClassifier.classify(digits)).isEqualTo(expected);
    }
}

package my.scamcheck.core.extract;

import static org.assertj.core.api.Assertions.assertThat;

import my.scamcheck.core.model.AccountCandidate;
import my.scamcheck.core.model.PhoneCandidate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NumberExtractorTest {

    private final NumberExtractor extractor = new NumberExtractor();

    @ParameterizedTest
    @ValueSource(strings = {"012-345 6789", "0123456789", "+60123456789", "+60 12-345 6789",
            "011-2345 6789", "60123456789", "03-1234 5678", "1300 88 1234", "+65 6123 4567",
            "0065 6123 4567"})
    void recognisesPhoneNumbers(String number) {
        NumberExtractor.Result result = extractor.extract("Hubungi " + number + " sekarang");

        assertThat(result.phones()).extracting(PhoneCandidate::text).containsExactly(number);
        assertThat(result.accounts()).isEmpty();
    }

    @Test
    void keepsThePlusInDigits() {
        assertThat(extractor.extract("+60 12-345 6789").phones())
                .extracting(PhoneCandidate::digits)
                .containsExactly("+60123456789");
    }

    @ParameterizedTest
    @ValueSource(strings = {"5123 4567 8901", "512345678901", "7012-3456-78", "1234567890123456"})
    void recognisesAccountNumbers(String number) {
        NumberExtractor.Result result = extractor.extract("Bank in ke akaun " + number + " ya");

        assertThat(result.accounts()).extracting(AccountCandidate::text).containsExactly(number);
        assertThat(result.phones()).isEmpty();
    }

    @Test
    void phoneDigitsAreNeverAlsoAnAccount() { // SRS US-06 AC3
        NumberExtractor.Result result = extractor.extract("0123456789 and 512345678901");

        assertThat(result.phones()).extracting(PhoneCandidate::digits).containsExactly("0123456789");
        assertThat(result.accounts()).extracting(AccountCandidate::digits).containsExactly("512345678901");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Tarikh 2026-11-24",                 // date
            "Kod TAC 123456",                    // short code
            "RM1,180,000",                       // amount
            "https://example.com/0123456789",    // inside a link
            "ref=5123456789012",                 // inside a query string
            "Versi 12.34.5678.9012"})            // dotted numbers
    void ignoresOtherNumbers(String text) {
        NumberExtractor.Result result = extractor.extract(text);

        assertThat(result.phones()).isEmpty();
        assertThat(result.accounts()).isEmpty();
    }

    @Test
    void splitsTwoNumbersThatRunTogether() {
        NumberExtractor.Result result = extractor.extract("0123456789 5123456789012");

        assertThat(result.phones()).extracting(PhoneCandidate::digits).containsExactly("0123456789");
        assertThat(result.accounts()).extracting(AccountCandidate::digits).containsExactly("5123456789012");
    }

    @Test
    void reportsTheSameNumberOnceWhateverItsFormatting() {
        assertThat(extractor.extract("012-345 6789 atau 0123456789").phones()).hasSize(1);
    }
}

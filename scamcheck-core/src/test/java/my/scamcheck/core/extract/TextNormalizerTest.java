package my.scamcheck.core.extract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.Locale;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TextNormalizerTest {

    static Stream<Arguments> samples() {
        return Stream.of(
                Arguments.of("SEGERA", "segera"),
                Arguments.of("Akaun  Dibekukan", "akaun dibekukan"),
                Arguments.of("  act\tnow\n please  ", "act now please"),
                Arguments.of("ＳＥＧＥＲＡ", "segera"),                // full-width letters
                Arguments.of("akaun dibekukan", "akaun dibekukan"), // no-break space
                Arguments.of("Before it’s too late", "before it's too late")); // curly apostrophe
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("samples")
    void normalisesCaseSpacingAndUnicodeForms(String input, String expected) {
        assertThat(TextNormalizer.normalize(input)).isEqualTo(expected);
    }

    @Test
    void lowerCasesIndependentlyOfTheDefaultLocale() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr")); // Turkish lower-cases "I" to dotless "ı"
            assertThat(TextNormalizer.normalize("PIN")).isEqualTo("pin");
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    void rejectsNull() {
        assertThatNullPointerException().isThrownBy(() -> TextNormalizer.normalize(null));
    }
}

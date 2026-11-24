package my.scamcheck.core.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AnalysisInputTest {

    @Test
    void acceptsTextUpToTheMaximumLength() {
        assertThat(new AnalysisInput("a".repeat(AnalysisInput.MAX_LENGTH)).text())
                .hasSize(AnalysisInput.MAX_LENGTH);
    }

    @Test
    void rejectsTextOverTheMaximumLengthWithoutEchoingIt() {
        String tooLong = "secret".repeat(1_000);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new AnalysisInput(tooLong))
                .withMessageNotContaining("secret");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\n\t "})
    void rejectsBlankText(String blank) {
        assertThatIllegalArgumentException().isThrownBy(() -> new AnalysisInput(blank));
    }

    @Test
    void rejectsNull() {
        assertThatNullPointerException().isThrownBy(() -> new AnalysisInput(null));
    }

    @Test
    void toStringDoesNotRevealText() {
        assertThat(new AnalysisInput("my account 1234567890").toString())
                .isEqualTo("AnalysisInput[length=21]");
    }
}

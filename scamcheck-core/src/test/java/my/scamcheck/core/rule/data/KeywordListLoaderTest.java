package my.scamcheck.core.rule.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import my.scamcheck.core.model.Weight;
import my.scamcheck.core.rule.KeywordEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class KeywordListLoaderTest {

    @Test
    void parsesEntriesSkippingCommentsAndBlankLines() {
        List<KeywordEntry> entries = KeywordListLoader.parse("test.txt", List.of(
                "# a comment",
                "",
                "STRONG   | ms | Dijamin  Untung",
                "  weak | EN | profit  "));

        assertThat(entries).containsExactly(
                new KeywordEntry("dijamin untung", Weight.STRONG, "ms"),
                new KeywordEntry("profit", Weight.WEAK, "en"));
    }

    @Test
    void storesPhrasesNormalised() {
        List<KeywordEntry> entries = KeywordListLoader.parse("test.txt", List.of("MODERATE | ms | AKAUN   Dibekukan"));

        assertThat(entries.getFirst().phrase()).isEqualTo("akaun dibekukan");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "STRONG | ms",                     // too few fields
            "STRONG | ms | a | b",             // too many fields
            "HUGE | ms | untung",              // unknown weight
            "STRONG | fr | profit",            // unknown language
            "STRONG | ms |   "                 // empty phrase
    })
    void rejectsMalformedLinesNamingFileAndLine(String badLine) {
        assertThatThrownBy(() -> KeywordListLoader.parse("bad.txt", List.of("# header", badLine)))
                .isInstanceOf(RuleDataException.class)
                .hasMessageStartingWith("bad.txt:2: ");
    }

    @Test
    void rejectsDuplicatePhrasesEvenWhenSpacingOrCaseDiffers() {
        assertThatThrownBy(() -> KeywordListLoader.parse("dup.txt", List.of(
                "STRONG | ms | akaun dibekukan",
                "WEAK   | ms | Akaun  DIBEKUKAN")))
                .isInstanceOf(RuleDataException.class)
                .hasMessageContaining("dup.txt:2")
                .hasMessageContaining("duplicate phrase");
    }

    @Test
    void rejectsFileWithoutKeywords() {
        assertThatThrownBy(() -> KeywordListLoader.parse("empty.txt", List.of("# only comments", "")))
                .isInstanceOf(RuleDataException.class)
                .hasMessageContaining("no keywords");
    }

    @Test
    void reportsMissingResource() {
        assertThatThrownBy(() -> KeywordListLoader.loadResource("/rules/keywords/does-not-exist.txt"))
                .isInstanceOf(RuleDataException.class)
                .hasMessageContaining("not found");
    }
}

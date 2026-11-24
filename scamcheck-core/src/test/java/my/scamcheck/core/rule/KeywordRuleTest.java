package my.scamcheck.core.rule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.List;
import my.scamcheck.core.extract.InputExtractor;
import my.scamcheck.core.model.AnalysisInput;
import my.scamcheck.core.model.Category;
import my.scamcheck.core.model.Finding;
import my.scamcheck.core.model.Weight;
import org.junit.jupiter.api.Test;

/** Behaviour of the generic keyword matcher, independent of the shipped data files. */
class KeywordRuleTest {

    private final InputExtractor extractor = new InputExtractor();

    private final KeywordRule rule = new KeywordRule("keyword.test", Category.INVESTMENT, List.of(
            new KeywordEntry("dijamin untung", Weight.STRONG, "ms"),
            new KeywordEntry("untung", Weight.WEAK, "ms"),
            new KeywordEntry("act now", Weight.MODERATE, "en")));

    private List<Finding> evaluate(String text) {
        return rule.evaluate(extractor.extract(new AnalysisInput(text)));
    }

    @Test
    void reportsMatchedPhraseWithRuleMetadata() {
        assertThat(evaluate("Please act now"))
                .singleElement()
                .satisfies(f -> {
                    assertThat(f.ruleId()).isEqualTo("keyword.test");
                    assertThat(f.category()).isEqualTo(Category.INVESTMENT);
                    assertThat(f.weight()).isEqualTo(Weight.MODERATE);
                    assertThat(f.matchedText()).isEqualTo("act now");
                    assertThat(f.messageKey()).isEqualTo("finding.keyword.test");
                });
    }

    @Test
    void returnsNothingWhenNoPhraseMatches() {
        assertThat(evaluate("Jom makan tengah hari")).isEmpty();
    }

    @Test
    void ignoresCaseAndExtraSpaces() {
        assertThat(evaluate("ACT   NOW!")).extracting(Finding::matchedText).containsExactly("act now");
    }

    @Test
    void matchesWholeWordsOnly() {
        assertThat(evaluate("Kami beruntung hari ini")).isEmpty();
        assertThat(evaluate("untungnya")).isEmpty();
        assertThat(evaluate("react now")).isEmpty();
    }

    @Test
    void matchesNextToPunctuation() {
        assertThat(evaluate("(untung!)")).extracting(Finding::matchedText).containsExactly("untung");
    }

    @Test
    void dropsShorterPhraseInsideLongerMatch() {
        assertThat(evaluate("Pelaburan dijamin untung"))
                .extracting(Finding::matchedText)
                .containsExactly("dijamin untung");
    }

    @Test
    void keepsShorterPhraseWhenItAlsoAppearsElsewhere() {
        assertThat(evaluate("Dijamin untung. Memang untung."))
                .extracting(Finding::matchedText)
                .containsExactly("dijamin untung", "untung");
    }

    @Test
    void reportsEachPhraseOnceHoweverOftenItAppears() {
        assertThat(evaluate("act now act now ACT NOW")).hasSize(1);
    }

    @Test
    void ordersFindingsStrongestFirst() {
        assertThat(evaluate("untung? act now"))
                .extracting(Finding::weight)
                .containsExactly(Weight.MODERATE, Weight.WEAK);
    }

    @Test
    void requiresAtLeastOneKeyword() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new KeywordRule("keyword.empty", Category.URGENCY, List.of()));
    }
}

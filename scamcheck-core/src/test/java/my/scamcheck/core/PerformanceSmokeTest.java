package my.scamcheck.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import my.scamcheck.core.extract.InputExtractor;
import my.scamcheck.core.model.AnalysisInput;
import my.scamcheck.core.rule.DefaultRules;
import my.scamcheck.core.score.RiskScorer;
import my.scamcheck.core.score.ScoringPolicy;
import org.junit.jupiter.api.Test;

/**
 * A guard against accidental slowdowns (e.g. a catastrophic regex), not a
 * benchmark. NFR-01 asks for p95 under 500 ms per request end to end; the
 * engine alone should be far below that even for the largest allowed input.
 */
class PerformanceSmokeTest {

    private static final int RUNS = 200;
    private static final long ENGINE_P95_BUDGET_MS = 50;

    @Test
    void largestInputIsAnalysedWellWithinBudget() {
        var analyzer = new ScamAnalyzer(new InputExtractor(), DefaultRules.all(), new RiskScorer(ScoringPolicy.defaults()));
        String chunk = "Akaun anda dibekukan! Klik maybank2u-verify.xyz/login, hubungi 012-345 678 atau bank in ke 5123 4567 8901 segera. ";
        var input = new AnalysisInput(chunk.repeat(AnalysisInput.MAX_LENGTH / chunk.length()));

        for (int i = 0; i < 20; i++) {
            analyzer.analyze(input); // warm up the JIT
        }
        long[] millis = new long[RUNS];
        for (int i = 0; i < RUNS; i++) {
            long start = System.nanoTime();
            analyzer.analyze(input);
            millis[i] = (System.nanoTime() - start) / 1_000_000;
        }
        Arrays.sort(millis);
        long p95 = millis[(int) Math.ceil(RUNS * 0.95) - 1];

        System.out.printf("Engine p95 for a %d-character input: %d ms%n", input.text().length(), p95);
        assertThat(p95).isLessThanOrEqualTo(ENGINE_P95_BUDGET_MS);
    }
}

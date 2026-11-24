package my.scamcheck.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import my.scamcheck.core.extract.InputExtractor;
import my.scamcheck.core.model.AnalysisInput;
import my.scamcheck.core.model.Finding;
import my.scamcheck.core.model.RiskAssessment;
import my.scamcheck.core.model.RiskLevel;
import my.scamcheck.core.rule.DefaultRules;
import my.scamcheck.core.score.RiskScorer;
import my.scamcheck.core.score.ScoringPolicy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Runs the full engine over the sample sets and checks the accuracy goals
 * (Project Plan G1/G2, SRS NFR-02/NFR-03, ADR-007 Q5):
 * <ul>
 *   <li>at least 90% of scam samples are rated MEDIUM or HIGH;</li>
 *   <li>at most 10% of legitimate samples are rated HIGH.</li>
 * </ul>
 * Prints a per-sample report so tuning changes can be compared run to run.
 */
class SampleSetAccuracyTest {

    private static final double MIN_SCAM_DETECTION = 0.90;
    private static final double MAX_LEGIT_HIGH = 0.10;
    private static final int MIN_SAMPLES_PER_SET = 30;

    private static ScamAnalyzer analyzer;

    @BeforeAll
    static void createAnalyzer() {
        analyzer = new ScamAnalyzer(new InputExtractor(), DefaultRules.all(), new RiskScorer(ScoringPolicy.defaults()));
    }

    record Result(String file, RiskAssessment assessment) {
        RiskLevel level() {
            return assessment.level();
        }

        String line() {
            String rules = assessment.findings().stream().map(Finding::ruleId).distinct().collect(Collectors.joining(", "));
            return "  %-6s %3d  %-42s %s".formatted(level(), assessment.score(), file, rules);
        }
    }

    @Test
    void g1_atLeast90PercentOfScamsAreMediumOrHigh() throws Exception {
        List<Result> results = analyzeAll("scam");
        long detected = results.stream().filter(r -> r.level() != RiskLevel.LOW).count();

        report("scam", results, "MEDIUM or HIGH", detected);
        assertThat(results).hasSizeGreaterThanOrEqualTo(MIN_SAMPLES_PER_SET);
        assertThat((double) detected / results.size())
                .as("share of scam samples rated MEDIUM or HIGH; missed: %s", files(results, RiskLevel.LOW))
                .isGreaterThanOrEqualTo(MIN_SCAM_DETECTION);
    }

    @Test
    void g2_atMost10PercentOfLegitimateMessagesAreHigh() throws Exception {
        List<Result> results = analyzeAll("legit");
        long high = results.stream().filter(r -> r.level() == RiskLevel.HIGH).count();

        report("legit", results, "HIGH", high);
        assertThat(results).hasSizeGreaterThanOrEqualTo(MIN_SAMPLES_PER_SET);
        assertThat((double) high / results.size())
                .as("share of legitimate samples rated HIGH; false alarms: %s", files(results, RiskLevel.HIGH))
                .isLessThanOrEqualTo(MAX_LEGIT_HIGH);
    }

    private static List<Result> analyzeAll(String set) throws IOException, URISyntaxException {
        Path folder = Path.of(Objects.requireNonNull(
                SampleSetAccuracyTest.class.getResource("/samples/" + set), "samples/" + set).toURI());
        try (Stream<Path> files = Files.list(folder)) {
            return files.filter(path -> path.toString().endsWith(".txt"))
                    .sorted()
                    .map(path -> new Result(path.getFileName().toString(), analyzer.analyze(new AnalysisInput(body(path)))))
                    .toList();
        }
    }

    /** The sample text without its {@code #} header lines. */
    private static String body(Path path) {
        try {
            return Files.readAllLines(path, StandardCharsets.UTF_8).stream()
                    .filter(line -> !line.startsWith("#"))
                    .collect(Collectors.joining("\n"))
                    .strip();
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + path, e);
        }
    }

    private static void report(String set, List<Result> results, String counted, long count) {
        System.out.printf("%nSample set '%s': %d/%d (%.0f%%) rated %s%n",
                set, count, results.size(), 100.0 * count / results.size(), counted);
        results.forEach(result -> System.out.println(result.line()));
    }

    private static List<String> files(List<Result> results, RiskLevel level) {
        return results.stream().filter(r -> r.level() == level).map(Result::file).toList();
    }
}

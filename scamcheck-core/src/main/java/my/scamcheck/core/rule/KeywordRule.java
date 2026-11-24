package my.scamcheck.core.rule;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import my.scamcheck.core.model.Category;
import my.scamcheck.core.model.ExtractedInput;
import my.scamcheck.core.model.Finding;

/**
 * Finds scam phrases from one keyword list in the normalised input (SRS US-03).
 *
 * <p>Matching rules:
 * <ul>
 *   <li>Case, spacing and Unicode forms are already normalised by the extractor (AC5).</li>
 *   <li>Phrases match whole words only: "untung" matches, "beruntung" doesn't (AC6).</li>
 *   <li>A phrase inside a longer matched phrase is dropped: in "dijamin untung" only the
 *       longer phrase is reported, not "untung" as well.</li>
 *   <li>Each distinct phrase is reported once, however often it appears.</li>
 * </ul>
 */
public final class KeywordRule implements Rule {

    /** Neither a letter nor a digit may touch the phrase on either side. */
    private static final String WORD_START = "(?<![\\p{L}\\p{N}])";
    private static final String WORD_END = "(?![\\p{L}\\p{N}])";

    private final String id;
    private final Category category;
    private final List<CompiledEntry> entries;

    public KeywordRule(String id, Category category, List<KeywordEntry> entries) {
        this.id = Objects.requireNonNull(id, "id");
        this.category = Objects.requireNonNull(category, "category");
        if (entries.isEmpty()) {
            throw new IllegalArgumentException("rule " + id + " needs at least one keyword");
        }
        this.entries = entries.stream().map(CompiledEntry::of).toList();
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Category category() {
        return category;
    }

    @Override
    public List<Finding> evaluate(ExtractedInput input) {
        List<Match> matches = findAllMatches(input.normalizedText());
        List<Match> accepted = dropMatchesInsideLongerOnes(matches);

        Set<String> reportedPhrases = new HashSet<>();
        return accepted.stream()
                .sorted(Comparator.comparing((Match m) -> m.entry().weight()).reversed()
                        .thenComparingInt(Match::start))
                .filter(m -> reportedPhrases.add(m.entry().phrase()))
                .map(this::toFinding)
                .toList();
    }

    private List<Match> findAllMatches(String text) {
        List<Match> matches = new ArrayList<>();
        for (CompiledEntry compiled : entries) {
            Matcher matcher = compiled.pattern().matcher(text);
            while (matcher.find()) {
                matches.add(new Match(matcher.start(), matcher.end(), compiled.entry()));
            }
        }
        return matches;
    }

    private static List<Match> dropMatchesInsideLongerOnes(List<Match> matches) {
        List<Match> byLengthDescending = matches.stream()
                .sorted(Comparator.comparingInt(Match::length).reversed()
                        .thenComparing((Match m) -> m.entry().weight(), Comparator.reverseOrder()))
                .toList();
        List<Match> accepted = new ArrayList<>();
        for (Match candidate : byLengthDescending) {
            if (accepted.stream().noneMatch(candidate::overlaps)) {
                accepted.add(candidate);
            }
        }
        return accepted;
    }

    private Finding toFinding(Match match) {
        KeywordEntry entry = match.entry();
        return new Finding(id, category, entry.weight(), entry.phrase(), "finding." + id, List.of());
    }

    @Override
    public String toString() {
        return "KeywordRule[id=" + id + ", keywords=" + entries.size() + "]";
    }

    private record CompiledEntry(KeywordEntry entry, Pattern pattern) {
        static CompiledEntry of(KeywordEntry entry) {
            return new CompiledEntry(entry, Pattern.compile(WORD_START + Pattern.quote(entry.phrase()) + WORD_END));
        }
    }

    private record Match(int start, int end, KeywordEntry entry) {
        int length() {
            return end - start;
        }

        boolean overlaps(Match other) {
            return start < other.end && other.start < end;
        }
    }
}

package my.scamcheck.api.analyze;

import java.util.List;
import my.scamcheck.core.model.Category;
import my.scamcheck.core.model.RiskLevel;
import my.scamcheck.core.model.Weight;

/**
 * Response of {@code POST /api/v1/analyze} (docs/api/openapi.yaml).
 *
 * <p>Matched text is part of the user's input, so no {@code toString} here
 * reveals it: Spring MVC logs response bodies via toString at DEBUG level (ADR-008).
 */
public record AnalyzeResponse(
        RiskLevel riskLevel,
        int score,
        String summary,
        List<FindingDto> findings,
        OfficialHelp officialHelp,
        String disclaimer) {

    public AnalyzeResponse {
        findings = List.copyOf(findings);
    }

    @Override
    public String toString() {
        return "AnalyzeResponse[riskLevel=" + riskLevel + ", score=" + score + ", findings=" + findings.size() + "]";
    }

    public record FindingDto(
            String ruleId,
            Category category,
            Weight weight,
            String matchedText,
            String title,
            String explanation) {

        @Override
        public String toString() {
            return "FindingDto[ruleId=" + ruleId + ", weight=" + weight + "]";
        }
    }

    public record OfficialHelp(String tip, List<HelpLink> links) {

        public OfficialHelp {
            links = List.copyOf(links);
        }
    }

    public record HelpLink(String id, String label, String url) {
    }
}

package my.scamcheck.api.analyze;

import java.util.List;
import java.util.Locale;
import my.scamcheck.api.analyze.AnalyzeResponse.FindingDto;
import my.scamcheck.api.analyze.AnalyzeResponse.HelpLink;
import my.scamcheck.api.analyze.AnalyzeResponse.OfficialHelp;
import my.scamcheck.core.model.Finding;
import my.scamcheck.core.model.RiskAssessment;
import my.scamcheck.core.model.RiskLevel;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

/**
 * Turns the core's language-free result into a localised response (ADR-009).
 *
 * <p>Message keys used:
 * <ul>
 *   <li>{@code <finding.messageKey>.title} and {@code .explanation}</li>
 *   <li>{@code summary.LOW|MEDIUM|HIGH}, plus {@code summary.LOW.minor} when LOW has weak findings</li>
 *   <li>{@code tip.LOW|MEDIUM|HIGH}, {@code help.semak-mule.label}, {@code help.nsrc-997.label}, {@code disclaimer}</li>
 * </ul>
 */
@Component
public class ResponseAssembler {

    static final String SEMAK_MULE_URL = "https://semakmule.rmp.gov.my";
    static final String NSRC_URL = "tel:997";

    private final MessageSource messages;

    public ResponseAssembler(MessageSource messages) {
        this.messages = messages;
    }

    public AnalyzeResponse assemble(RiskAssessment assessment, Language language) {
        Locale locale = language.locale();
        List<FindingDto> findings = assessment.findings().stream()
                .map(finding -> toDto(finding, locale))
                .toList();

        return new AnalyzeResponse(
                assessment.level(),
                assessment.score(),
                summary(assessment, locale),
                findings,
                officialHelp(assessment.level(), locale),
                message("disclaimer", locale));
    }

    private FindingDto toDto(Finding finding, Locale locale) {
        Object[] args = finding.messageArgs().toArray();
        return new FindingDto(
                finding.ruleId(),
                finding.category(),
                finding.weight(),
                finding.matchedText(),
                messages.getMessage(finding.messageKey() + ".title", args, locale),
                messages.getMessage(finding.messageKey() + ".explanation", args, locale));
    }

    private String summary(RiskAssessment assessment, Locale locale) {
        boolean lowWithMinorSigns = assessment.level() == RiskLevel.LOW && !assessment.findings().isEmpty();
        String key = lowWithMinorSigns ? "summary.LOW.minor" : "summary." + assessment.level();
        return message(key, locale);
    }

    private OfficialHelp officialHelp(RiskLevel level, Locale locale) {
        return new OfficialHelp(
                message("tip." + level, locale),
                List.of(
                        new HelpLink("semak-mule", message("help.semak-mule.label", locale), SEMAK_MULE_URL),
                        new HelpLink("nsrc-997", message("help.nsrc-997.label", locale), NSRC_URL)));
    }

    private String message(String key, Locale locale) {
        return messages.getMessage(key, null, locale);
    }
}

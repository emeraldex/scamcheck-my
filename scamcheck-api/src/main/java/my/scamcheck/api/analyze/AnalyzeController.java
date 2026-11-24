package my.scamcheck.api.analyze;

import java.util.concurrent.TimeUnit;
import my.scamcheck.core.ScamAnalyzer;
import my.scamcheck.core.model.AnalysisInput;
import my.scamcheck.core.model.Finding;
import my.scamcheck.core.model.RiskAssessment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** HTTP adapter for the analysis engine. Holds no business logic (ADR-003). */
@RestController
@RequestMapping("/api/v1")
public class AnalyzeController {

    private static final Logger log = LoggerFactory.getLogger(AnalyzeController.class);

    private final ScamAnalyzer analyzer;
    private final ResponseAssembler assembler;

    public AnalyzeController(ScamAnalyzer analyzer, ResponseAssembler assembler) {
        this.analyzer = analyzer;
        this.assembler = assembler;
    }

    @PostMapping(
            path = "/analyze",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public AnalyzeResponse analyze(@RequestBody AnalyzeRequest request) {
        request.validate();
        long start = System.nanoTime();

        Language language = request.languageOrDefault();
        RiskAssessment assessment = analyzer.analyze(new AnalysisInput(request.text()));
        AnalyzeResponse response = assembler.assemble(assessment, language);

        // Metadata only: never the input or matched text (ADR-008).
        log.info("analysis length={} language={} level={} score={} rules={} durationMs={}",
                request.text().length(),
                language.code(),
                assessment.level(),
                assessment.score(),
                assessment.findings().stream().map(Finding::ruleId).distinct().toList(),
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));

        return response;
    }
}

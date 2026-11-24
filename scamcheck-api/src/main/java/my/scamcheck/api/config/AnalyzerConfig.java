package my.scamcheck.api.config;

import my.scamcheck.core.ScamAnalyzer;
import my.scamcheck.core.extract.InputExtractor;
import my.scamcheck.core.rule.DefaultRules;
import my.scamcheck.core.score.RiskScorer;
import my.scamcheck.core.score.ScoringPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The only place the framework-free core meets Spring (ADR-003, ADR-004).
 * Adding a rule means adding it to the list here.
 */
@Configuration(proxyBeanMethods = false)
public class AnalyzerConfig {

    @Bean
    ScamAnalyzer scamAnalyzer() {
        return new ScamAnalyzer(
                new InputExtractor(),
                DefaultRules.all(),
                new RiskScorer(ScoringPolicy.defaults()));
    }
}

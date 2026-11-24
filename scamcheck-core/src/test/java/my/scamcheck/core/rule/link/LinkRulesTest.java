package my.scamcheck.core.rule.link;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import my.scamcheck.core.extract.InputExtractor;
import my.scamcheck.core.model.AnalysisInput;
import my.scamcheck.core.model.Category;
import my.scamcheck.core.model.ExtractedInput;
import my.scamcheck.core.model.Finding;
import my.scamcheck.core.model.Weight;
import my.scamcheck.core.rule.Rule;
import my.scamcheck.core.rule.data.BankDomains;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Each link rule against SRS US-04. Rules get small hand-made data so tests don't depend on the shipped lists. */
class LinkRulesTest {

    private static final InputExtractor EXTRACTOR = new InputExtractor();

    private static List<Finding> evaluate(Rule rule, String text) {
        ExtractedInput input = EXTRACTOR.extract(new AnalysisInput(text));
        return rule.evaluate(input);
    }

    @Nested
    class LookalikeBank {

        private final Rule rule = new LookalikeBankRule(List.of(
                new BankDomains("Maybank", Set.of("maybank", "maybank2u"), Set.of("maybank2u.com.my", "maybank.com")),
                new BankDomains("CIMB Bank", Set.of("cimb"), Set.of("cimb.com.my"))));

        @Test
        void us04_ac1_brandNameOnAnotherDomain() {
            assertThat(evaluate(rule, "https://maybank2u-secure.xyz/login"))
                    .singleElement()
                    .satisfies(f -> {
                        assertThat(f.ruleId()).isEqualTo("link.lookalike-bank");
                        assertThat(f.category()).isEqualTo(Category.LINK);
                        assertThat(f.weight()).isEqualTo(Weight.STRONG);
                        assertThat(f.matchedText()).isEqualTo("maybank2u-secure.xyz");
                        assertThat(f.messageArgs()).containsExactly("Maybank");
                    });
        }

        @Test
        void us04_ac2_genuineDomainsAreNotFlagged() {
            assertThat(evaluate(rule, "https://www.maybank2u.com.my")).isEmpty();
            assertThat(evaluate(rule, "maybank.com/personal")).isEmpty();
            assertThat(evaluate(rule, "https://www.cimb.com.my/en")).isEmpty();
        }

        @Test
        void brandAsSubdomainOfAnotherSiteIsFlagged() {
            assertThat(evaluate(rule, "http://cimb.login-portal.top")).extracting(Finding::messageArgs)
                    .containsExactly(List.of("CIMB Bank"));
        }

        @Test
        void oneCharacterTypoOfOfficialDomainIsFlagged() {
            assertThat(evaluate(rule, "https://maybamk2u.com.my")).hasSize(1);
        }

        @Test
        void unrelatedSitesAreNotFlagged() {
            assertThat(evaluate(rule, "https://example.com https://shopee.com.my")).isEmpty();
        }

        @Test
        void userInfoTrickIsFlagged() {
            assertThat(evaluate(rule, "https://maybank2u.com.my@maybank2u-verify.top/x"))
                    .extracting(Finding::matchedText)
                    .containsExactly("maybank2u-verify.top");
        }
    }

    @Nested
    class Shortener {

        private final Rule rule = new ShortenerRule(Set.of("bit.ly", "tinyurl.com"));

        @Test
        void us04_ac3_knownShortener() {
            assertThat(evaluate(rule, "Klik bit.ly/abc"))
                    .singleElement()
                    .satisfies(f -> {
                        assertThat(f.weight()).isEqualTo(Weight.MODERATE);
                        assertThat(f.matchedText()).isEqualTo("bit.ly");
                    });
        }

        @Test
        void sameShortenerReportedOnce() {
            assertThat(evaluate(rule, "bit.ly/a bit.ly/b https://www.tinyurl.com/c")).hasSize(2);
        }

        @Test
        void normalLinksAreNotFlagged() {
            assertThat(evaluate(rule, "https://example.com/bit.ly")).isEmpty();
        }
    }

    @Nested
    class UnusualTld {

        private final Rule rule = new UnusualTldRule(Set.of("xyz", "top"));

        @Test
        void us04_ac4_unusualEnding() {
            assertThat(evaluate(rule, "visit promo-hadiah.xyz"))
                    .singleElement()
                    .satisfies(f -> {
                        assertThat(f.weight()).isEqualTo(Weight.WEAK);
                        assertThat(f.messageArgs()).containsExactly(".xyz");
                    });
        }

        @Test
        void commonEndingsAreNotFlagged() {
            assertThat(evaluate(rule, "https://www.maybank2u.com.my and example.com")).isEmpty();
        }
    }

    @Nested
    class RawIp {

        private final Rule rule = new RawIpRule();

        @Test
        void us04_ac5_rawIpLink() {
            assertThat(evaluate(rule, "Log masuk di http://192.0.2.10/bank"))
                    .singleElement()
                    .satisfies(f -> {
                        assertThat(f.weight()).isEqualTo(Weight.MODERATE);
                        assertThat(f.matchedText()).isEqualTo("192.0.2.10");
                    });
        }

        @Test
        void namedHostsAreNotFlagged() {
            assertThat(evaluate(rule, "https://example.com")).isEmpty();
        }
    }

    @Nested
    class NotHttps {

        private final Rule rule = new NotHttpsRule();

        @Test
        void us04_ac6_plainHttp() {
            assertThat(evaluate(rule, "http://example.com/login"))
                    .singleElement()
                    .satisfies(f -> {
                        assertThat(f.weight()).isEqualTo(Weight.WEAK);
                        assertThat(f.matchedText()).isEqualTo("http://example.com");
                    });
        }

        @Test
        void httpsAndSchemelessAreNotFlagged() {
            assertThat(evaluate(rule, "https://example.com and example.com")).isEmpty();
        }
    }

    @Test
    void defaultsLoadFiveLinkRules() {
        assertThat(LinkRules.defaults()).extracting(Rule::id).containsExactly(
                "link.lookalike-bank", "link.shortener", "link.unusual-tld", "link.raw-ip", "link.not-https");
    }

    @Test
    void textWithoutLinksTriggersNoLinkRule() {
        ExtractedInput input = EXTRACTOR.extract(new AnalysisInput("Jom makan tengah hari esok?"));
        assertThat(LinkRules.defaults()).allSatisfy(rule -> assertThat(rule.evaluate(input)).isEmpty());
    }
}

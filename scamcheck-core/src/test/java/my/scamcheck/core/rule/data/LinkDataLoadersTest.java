package my.scamcheck.core.rule.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class LinkDataLoadersTest {

    @Test
    void simpleListLowerCasesAndSkipsComments() {
        assertThat(SimpleListLoader.parse("t.txt", List.of("# c", "", " Bit.LY ", "xyz")))
                .containsExactlyInAnyOrder("bit.ly", "xyz");
    }

    @Test
    void simpleListRejectsDuplicatesSpacesAndEmptyFiles() {
        assertThatThrownBy(() -> SimpleListLoader.parse("t.txt", List.of("xyz", "XYZ")))
                .isInstanceOf(RuleDataException.class).hasMessageContaining("t.txt:2");
        assertThatThrownBy(() -> SimpleListLoader.parse("t.txt", List.of("bit ly")))
                .isInstanceOf(RuleDataException.class).hasMessageContaining("spaces");
        assertThatThrownBy(() -> SimpleListLoader.parse("t.txt", List.of("# only")))
                .isInstanceOf(RuleDataException.class).hasMessageContaining("no values");
    }

    @Test
    void bankDomainsParsesNameTokensAndDomains() {
        List<BrandDomains> banks = BrandDomainsLoader.parse("b.txt", List.of(
                "# header",
                "Maybank | maybank, Maybank2u | maybank2u.com.my, maybank.com"));

        assertThat(banks).singleElement().satisfies(bank -> {
            assertThat(bank.displayName()).isEqualTo("Maybank");
            assertThat(bank.brandTokens()).containsExactlyInAnyOrder("maybank", "maybank2u");
            assertThat(bank.officialDomains()).containsExactlyInAnyOrder("maybank2u.com.my", "maybank.com");
        });
    }

    @Test
    void bankDomainsRejectsMalformedLinesAndSharedDomains() {
        assertThatThrownBy(() -> BrandDomainsLoader.parse("b.txt", List.of("Maybank | maybank")))
                .isInstanceOf(RuleDataException.class).hasMessageContaining("b.txt:1");
        assertThatThrownBy(() -> BrandDomainsLoader.parse("b.txt", List.of("Maybank |  | maybank.com")))
                .isInstanceOf(RuleDataException.class).hasMessageContaining("must all be present");
        assertThatThrownBy(() -> BrandDomainsLoader.parse("b.txt", List.of(
                "A | a | shared.com",
                "B | b | shared.com")))
                .isInstanceOf(RuleDataException.class).hasMessageContaining("b.txt:2");
    }

    @Test
    void shippedLinkDataFilesLoad() {
        assertThat(BrandDomainsLoader.loadResource("/rules/bank-domains.txt")).hasSizeGreaterThanOrEqualTo(10);
        assertThat(SimpleListLoader.loadResource("/rules/url-shorteners.txt")).contains("bit.ly");
        assertThat(SimpleListLoader.loadResource("/rules/unusual-tlds.txt")).contains("xyz");
        assertThat(SimpleListLoader.loadResource("/rules/tlds.txt")).contains("com", "my");
    }
}

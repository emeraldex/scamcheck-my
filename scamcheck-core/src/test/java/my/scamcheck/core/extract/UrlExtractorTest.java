package my.scamcheck.core.extract;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import my.scamcheck.core.model.UrlCandidate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UrlExtractorTest {

    private final UrlExtractor extractor = UrlExtractor.withDefaultTlds();

    private List<String> hosts(String text) {
        return extractor.extract(text).stream().map(UrlCandidate::host).toList();
    }

    @Test
    void parsesLinkWithScheme() {
        assertThat(extractor.extract("Login: https://Maybank2u-Secure.xyz/login?x=1 now"))
                .singleElement()
                .satisfies(url -> {
                    assertThat(url.text()).isEqualTo("https://Maybank2u-Secure.xyz/login?x=1");
                    assertThat(url.scheme()).isEqualTo("https");
                    assertThat(url.host()).isEqualTo("maybank2u-secure.xyz");
                    assertThat(url.ipAddress()).isFalse();
                });
    }

    @Test
    void detectsLinksWithoutScheme() { // SRS US-04 AC7
        assertThat(extractor.extract("Pergi ke maybank2u-secure.xyz sekarang"))
                .singleElement()
                .satisfies(url -> {
                    assertThat(url.scheme()).isNull();
                    assertThat(url.host()).isEqualTo("maybank2u-secure.xyz");
                });
        assertThat(hosts("www.example.test/path")).containsExactly("www.example.test");
        assertThat(hosts("bit.ly/abc123")).containsExactly("bit.ly");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ok.jom kita pergi",          // chat with a dot, unknown ending
            "Harga RM1.18 bilion",         // money
            "Versi 1.2.3.4 sudah keluar",  // version number, no path
            "Emel saya ali@example.com",   // email address
            "e.g. this one"})
    void ignoresTextThatIsNotALink(String text) {
        assertThat(extractor.extract(text)).isEmpty();
    }

    @Test
    void trimsTrailingPunctuation() {
        assertThat(extractor.extract("Klik (https://bit.ly/x1)."))
                .extracting(UrlCandidate::text)
                .containsExactly("https://bit.ly/x1");
        assertThat(hosts("Lawati maybank2u.com.my.")).containsExactly("maybank2u.com.my");
    }

    @Test
    void stripsPortAndUserInfoFromHost() {
        // A classic trick: everything before "@" is ignored by the browser.
        assertThat(hosts("https://maybank2u.com.my@evil.example.com:8443/login"))
                .containsExactly("evil.example.com");
    }

    @Test
    void recognisesRawIpAddresses() {
        assertThat(extractor.extract("http://192.0.2.10/login"))
                .singleElement()
                .satisfies(url -> {
                    assertThat(url.host()).isEqualTo("192.0.2.10");
                    assertThat(url.ipAddress()).isTrue();
                });
        assertThat(hosts("Buka 192.0.2.10:8080/bank")).containsExactly("192.0.2.10");
    }

    @Test
    void findsSeveralLinksInOrderWithoutDuplicates() {
        assertThat(hosts("bit.ly/a then https://example.com and bit.ly/a again"))
                .containsExactly("bit.ly", "example.com");
    }

    @Test
    void doesNotDoubleCountTheHostInsideASchemeLink() {
        assertThat(extractor.extract("https://www.maybank2u.com.my/home")).hasSize(1);
    }

    @Test
    void toStringDoesNotRevealTheLink() {
        assertThat(extractor.extract("https://secret.example.com").getFirst().toString())
                .doesNotContain("secret");
    }
}

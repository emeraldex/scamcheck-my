package my.scamcheck.core.extract;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DomainNamesTest {

    @ParameterizedTest
    @CsvSource({
            "www.maybank2u.com.my,  maybank2u.com.my",
            "maybank2u.com.my,      maybank2u.com.my",
            "a.b.example.com,       example.com",
            "maybank2u-secure.xyz,  maybank2u-secure.xyz",
            "login.portal.gov.my,   portal.gov.my",
            "hongleongconnect.my,   hongleongconnect.my",
            "bit.ly,                bit.ly"
    })
    void registrableDomain(String host, String expected) {
        assertThat(DomainNames.registrableDomain(host)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "maybank2u-secure.xyz, xyz",
            "www.maybank2u.com.my, my",
            "localhost,            localhost"
    })
    void lastLabel(String host, String expected) {
        assertThat(DomainNames.lastLabel(host)).isEqualTo(expected);
    }
}

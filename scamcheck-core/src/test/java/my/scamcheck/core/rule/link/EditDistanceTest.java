package my.scamcheck.core.rule.link;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EditDistanceTest {

    @ParameterizedTest(name = "{0} vs {1} -> {2}")
    @CsvSource({
            "maybamk2u.com.my, maybank2u.com.my, true",   // substitution
            "maybnk2u.com.my,  maybank2u.com.my, true",   // deletion
            "maybankk2u.com.my, maybank2u.com.my, true",  // insertion
            "maybank2u.com.myx, maybank2u.com.my, true",  // insertion at end
            "maybank2u.com.my, maybank2u.com.my, false",  // identical
            "maybnak2u.com.my, maybank2u.com.my, false",  // transposition = 2 edits
            "rnaybank2u.com.my, maybank2u.com.my, false", // m -> rn = 2 edits
            "cimb.com.my,      maybank2u.com.my, false"
    })
    void detectsExactlyOneEdit(String a, String b, boolean expected) {
        assertThat(EditDistance.isExactlyOne(a, b)).isEqualTo(expected);
        assertThat(EditDistance.isExactlyOne(b, a)).isEqualTo(expected);
    }
}

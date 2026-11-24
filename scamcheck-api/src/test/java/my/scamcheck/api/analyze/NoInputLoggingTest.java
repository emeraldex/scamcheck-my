package my.scamcheck.api.analyze;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcPrint;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/**
 * Input never appears in logs, even with Spring MVC and our own code at DEBUG
 * level (ADR-008, SRS US-10 AC2). Each request carries a unique canary string;
 * the test fails if the canary shows up anywhere in the captured output.
 */
@SpringBootTest(properties = {
        "logging.level.my.scamcheck=DEBUG",
        "logging.level.org.springframework.web=DEBUG"
})
// print = NONE: MockMvc's own request dump on test failure is harness output, not app logging.
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
@ExtendWith(OutputCaptureExtension.class)
class NoInputLoggingTest {

    private static final String CANARY = "zq7canary";

    @Autowired
    private MockMvcTester mvc;

    @Test
    void successfulAnalysisDoesNotLogInput(CapturedOutput output) {
        post("{\"text\": \"" + CANARY + " akaun anda dibekukan segera\"}", HttpStatus.OK);

        // The metadata line is there, so maintainers can see which rules fire...
        assertThat(output).contains("language=ms level=MEDIUM score=55");
        // ...but never the text itself.
        assertThat(output.getAll().toLowerCase()).doesNotContain(CANARY);
    }

    @Test
    void tooLongInputDoesNotLogInput(CapturedOutput output) {
        post("{\"text\": \"" + (CANARY + " ").repeat(600) + "\"}", HttpStatus.BAD_REQUEST);

        assertThat(output).contains("analysis rejected reason=validation-error field=text");
        assertThat(output.getAll().toLowerCase()).doesNotContain(CANARY);
    }

    @Test
    void malformedRequestDoesNotLogInput(CapturedOutput output) {
        post("{\"text\": \"" + CANARY + "\", \"language\": \"fr\"}", HttpStatus.BAD_REQUEST);

        assertThat(output).contains("analysis rejected reason=malformed-request");
        assertThat(output.getAll().toLowerCase()).doesNotContain(CANARY);
    }

    private void post(String json, HttpStatus expected) {
        assertThat(mvc.post().uri("/api/v1/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .exchange())
                .hasStatus(expected);
    }
}

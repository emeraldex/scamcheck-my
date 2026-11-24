package my.scamcheck.api.analyze;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** {@code POST /api/v1/analyze} against docs/api/openapi.yaml and the SRS. */
@SpringBootTest
@AutoConfigureMockMvc
class AnalyzeControllerTest {

    @Autowired
    private MockMvcTester mvc;

    private MvcTestResult post(String json) {
        return mvc.post().uri("/api/v1/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .exchange();
    }

    @Nested
    class Success {

        @Test
        void highRiskMessageReturnsLevelScoreAndExplainedFindings() { // US-01 AC1, US-02 AC4, US-07 AC1
            MvcTestResult result = post("""
                    {"text": "Pelaburan dijamin untung! Daftar segera dan berikan kod TAC.", "language": "en"}
                    """);

            assertThat(result).hasStatusOk().hasContentType(MediaType.APPLICATION_JSON);
            assertThat(result).bodyJson().extractingPath("$.riskLevel").isEqualTo("HIGH");
            assertThat(result).bodyJson().extractingPath("$.score").isEqualTo(90);
            assertThat(result).bodyJson().extractingPath("$.summary").isEqualTo("Several strong scam warning signs were found.");
            assertThat(result).bodyJson().extractingPath("$.findings[0].weight").isEqualTo("STRONG");
            assertThat(result).bodyJson().extractingPath("$.findings[0].title").asString().isNotBlank();
            assertThat(result).bodyJson().extractingPath("$.findings[0].explanation").asString().isNotBlank();
            assertThat(result).bodyJson().extractingPath("$.findings[*].ruleId").asArray()
                    .contains("keyword.investment", "keyword.urgency", "keyword.credential-request");
        }

        @Test
        void explanationsFollowTheRequestedLanguage() { // US-09 AC2, AC4
            String text = "Akaun anda dibekukan, hubungi segera";

            assertThat(post("{\"text\": \"" + text + "\", \"language\": \"en\"}"))
                    .bodyJson().extractingPath("$.findings[0].title").isEqualTo("Threat about your account");
            assertThat(post("{\"text\": \"" + text + "\", \"language\": \"ms\"}"))
                    .bodyJson().extractingPath("$.findings[0].title").isEqualTo("Ugutan tentang akaun anda");
        }

        @Test
        void languageDefaultsToMalay() { // US-09 AC1
            assertThat(post("{\"text\": \"Jom makan tengah hari esok?\"}"))
                    .bodyJson().extractingPath("$.summary")
                    .isEqualTo("Tiada tanda amaran biasa ditemui. Ini tidak bermakna ia selamat.");
        }

        @Test
        void lowResultNeverSaysSafeAndStillShowsOfficialHelp() { // US-02 AC2, US-08 AC1, US-12 AC1
            MvcTestResult result = post("{\"text\": \"See you at lunch tomorrow?\", \"language\": \"en\"}");

            assertThat(result).hasStatusOk();
            assertThat(result).bodyJson().extractingPath("$.riskLevel").isEqualTo("LOW");
            assertThat(result).bodyJson().extractingPath("$.findings").asArray().isEmpty();
            assertThat(result).bodyJson().extractingPath("$.summary")
                    .isEqualTo("No common warning signs found. This does not mean it is safe.");
            assertThat(result).bodyJson().extractingPath("$.officialHelp.tip").asString().isNotBlank();
            assertThat(result).bodyJson().extractingPath("$.officialHelp.links[0].url").isEqualTo("https://semakmule.rmp.gov.my");
            assertThat(result).bodyJson().extractingPath("$.officialHelp.links[1].url").isEqualTo("tel:997");
            assertThat(result).bodyJson().extractingPath("$.disclaimer").asString().startsWith("Not affiliated");
        }

        @Test
        void lowResultWithWeakSignsSaysMinorSigns() {
            assertThat(post("{\"text\": \"Jom makan, untung la dapat diskaun\", \"language\": \"en\"}"))
                    .bodyJson().extractingPath("$.summary")
                    .isEqualTo("Only minor warning signs were found. This does not mean it is safe.");
        }

        @Test
        void acceptsTextAtTheMaximumLength() { // US-01 AC4 boundary
            assertThat(post("{\"text\": \"" + "a".repeat(5_000) + "\"}")).hasStatusOk();
        }
    }

    @Nested
    class InvalidRequests {

        private void assertValidationProblem(MvcTestResult result, String expectedMessage) {
            assertThat(result).hasStatus(HttpStatus.BAD_REQUEST).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
            assertThat(result).bodyJson().extractingPath("$.type").isEqualTo("/problems/validation-error");
            assertThat(result).bodyJson().extractingPath("$.title").isEqualTo("Invalid request");
            assertThat(result).bodyJson().extractingPath("$.status").isEqualTo(400);
            assertThat(result).bodyJson().extractingPath("$.errors[0].field").isEqualTo("text");
            assertThat(result).bodyJson().extractingPath("$.errors[0].message").isEqualTo(expectedMessage);
        }

        @Test
        void blankTextIsRejected() { // US-01 AC3, US-11 AC2
            assertValidationProblem(post("{\"text\": \"   \"}"), "Text must not be blank.");
        }

        @Test
        void missingTextIsRejected() {
            assertValidationProblem(post("{\"language\": \"en\"}"), "Text must not be blank.");
        }

        @Test
        void tooLongTextIsRejectedWithoutEchoingIt() { // US-01 AC4, ADR-008
            MvcTestResult result = post("{\"text\": \"" + "secret ".repeat(800) + "\"}");

            assertValidationProblem(result, "Text must be 5,000 characters or fewer.");
            assertThat(result).bodyText().doesNotContain("secret");
        }

        @Test
        void malformedJsonIsRejected() {
            MvcTestResult result = post("{\"text\": ");

            assertThat(result).hasStatus(HttpStatus.BAD_REQUEST).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
            assertThat(result).bodyJson().extractingPath("$.type").isEqualTo("/problems/malformed-request");
        }

        @Test
        void unknownLanguageIsRejected() {
            assertThat(post("{\"text\": \"hello\", \"language\": \"fr\"}"))
                    .hasStatus(HttpStatus.BAD_REQUEST)
                    .bodyJson().extractingPath("$.type").isEqualTo("/problems/malformed-request");
        }

        @Test
        void unknownFieldIsRejected() {
            assertThat(post("{\"text\": \"hello\", \"extra\": true}"))
                    .hasStatus(HttpStatus.BAD_REQUEST)
                    .bodyJson().extractingPath("$.type").isEqualTo("/problems/malformed-request");
        }

        @Test
        void nonJsonContentTypeIsRejected() {
            MvcTestResult result = mvc.post().uri("/api/v1/analyze")
                    .contentType(MediaType.TEXT_PLAIN)
                    .content("hello")
                    .exchange();

            assertThat(result).hasStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }
    }
}

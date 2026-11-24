package my.scamcheck.api.ops;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** The host's health check works, and no other actuator endpoint is reachable (ADR-011, NFR-05). */
@SpringBootTest
@AutoConfigureMockMvc
class ActuatorExposureTest {

    @Autowired
    private MockMvcTester mvc;

    @Test
    void healthIsUpWithoutDetails() {
        assertThat(mvc.get().uri("/actuator/health"))
                .hasStatusOk()
                .bodyJson().isStrictlyEqualTo("{\"status\":\"UP\"}"); // no components, details or probe groups
    }

    @Test
    void healthResponseCarriesSecurityHeaders() {
        assertThat(mvc.get().uri("/actuator/health"))
                .headers().containsHeader("Content-Security-Policy");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "/actuator", "/actuator/env", "/actuator/beans", "/actuator/configprops",
        "/actuator/heapdump", "/actuator/threaddump", "/actuator/loggers", "/actuator/mappings",
        "/actuator/metrics", "/actuator/info", "/actuator/shutdown"
    })
    void otherEndpointsAreNotExposed(String path) {
        assertThat(mvc.get().uri(path)).hasStatus(HttpStatus.NOT_FOUND);
    }
}

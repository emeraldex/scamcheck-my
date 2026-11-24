package my.scamcheck.api.error;

import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * RFC 9457 problem responses for bad requests (docs/api/openapi.yaml).
 * Error bodies say what is wrong but never echo the submitted value (ADR-008).
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    static final URI VALIDATION_ERROR = URI.create("/problems/validation-error");
    static final URI MALFORMED_REQUEST = URI.create("/problems/malformed-request");

    @ExceptionHandler(InvalidRequestException.class)
    ProblemDetail handleInvalidRequest(InvalidRequestException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "The request has 1 invalid field.");
        problem.setType(VALIDATION_ERROR);
        problem.setTitle("Invalid request");
        problem.setProperty("errors", List.of(Map.of("field", ex.field(), "message", ex.getMessage())));
        return problem;
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "The request body must be valid JSON with a 'text' field and an optional 'language' of 'en' or 'ms'.");
        problem.setType(MALFORMED_REQUEST);
        problem.setTitle("Invalid request");
        return handleExceptionInternal(ex, problem, headers, HttpStatus.BAD_REQUEST, request);
    }
}

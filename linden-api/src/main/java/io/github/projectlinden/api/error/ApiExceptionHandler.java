package io.github.projectlinden.api.error;

import io.github.projectlinden.api.logging.CorrelationIdFilter;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Locale;

/**
 * The one place where exceptions become HTTP responses (ENGINEERING.md § 7).
 *
 * <p>Every error is a Problem Details response (RFC 9457) with a stable {@code code} and the request's
 * {@code correlationId}. Clients never see internals – no stack traces, SQL or exception messages. The cause of an
 * unexpected error is logged, in a line that carries the same correlation ID.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    /**
     * Errors Spring already knows (404, 405, 400, …): add the stable code and the correlation ID.
     */
    @Override
    protected @Nullable ResponseEntity<Object> handleExceptionInternal(Exception exception, @Nullable Object body,
                                                                       HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        ResponseEntity<Object> response = super.handleExceptionInternal(exception, body, headers, statusCode, request);
        if (response != null && response.getBody() instanceof ProblemDetail problem) {
            problem.setDetail(detailFor(statusCode));
            problem.setProperty("code", codeFor(statusCode));
            addCorrelationId(problem);
        }
        return response;
    }

    /**
     * Everything unexpected: 500, the cause only in the log, a calm message for the client.
     */
    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception exception) {
        LOGGER.error("Unexpected error while handling a request", exception);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong on our side. Please try again later.");
        problem.setTitle("Internal error");
        problem.setProperty("code", "internal_error");
        addCorrelationId(problem);
        return problem;
    }

    /**
     * A stable, machine-readable code derived from the status, e.g. 404 → "not_found".
     */
    private static String codeFor(HttpStatusCode statusCode) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        return status == null ? "error_" + statusCode.value() : status.name().toLowerCase(Locale.ROOT);
    }

    /**
     * A calm, generic text – Spring's own details can reveal internals (e.g. "No static resource …").
     */
    private static String detailFor(HttpStatusCode statusCode) {
        return statusCode.is4xxClientError()
                ? "The request could not be handled. Check the address and the data you sent."
                : "Something went wrong on our side. Please try again later.";
    }

    private static void addCorrelationId(ProblemDetail problem) {
        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
        if (correlationId != null) {
            problem.setProperty("correlationId", correlationId);
        }
    }
}

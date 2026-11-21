package io.github.projectlinden.api.logging;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CorrelationIdFilter} – no Spring context, just a simulated request.
 */
class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();
    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/me");
    private final MockHttpServletResponse response = new MockHttpServletResponse();
    /**
     * 8-4-4-4-12 hexadecimal digits, as produced by UUID.randomUUID().
     */
    private static final String UUID_FORMAT = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    /**
     * What the MDC contained while the request was running inside the filter chain.
     */
    private final AtomicReference<String> idDuringRequest = new AtomicReference<>();
    private final FilterChain chain = (_, _) -> idDuringRequest.set(MDC.get(CorrelationIdFilter.MDC_KEY));

    @AfterEach
    void clearMdc() {
        // Safety net: a failing test must not leave an ID behind for the next test on this thread
        MDC.clear();
    }

    @Test
    @DisplayName("no incoming id → a new UUID in the response header and in the MDC during the request")
    void noIncomingId_generatesUuid_inHeaderAndMdc() throws Exception {
        filter.doFilter(request, response, chain);

        String header = response.getHeader(CorrelationIdFilter.HEADER);
        assertThat(header).as("X-Correlation-Id header").isNotBlank();
        assertThat(header).as("header is a UUID").matches(UUID_FORMAT);
        assertThat(idDuringRequest.get()).as("MDC during the request").isEqualTo(header);
    }

    @Test
    @DisplayName("safe incoming id → reused in the response header and in the MDC")
    void safeIncomingId_isReused() throws Exception {
        request.addHeader(CorrelationIdFilter.HEADER, "pwa-1234-abcd");

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(CorrelationIdFilter.HEADER)).isEqualTo("pwa-1234-abcd");
        assertThat(idDuringRequest.get()).isEqualTo("pwa-1234-abcd");
    }

    @Test
    @DisplayName("unsafe incoming id (line break = forged log line) → replaced by a new UUID")
    void unsafeIncomingId_isReplaced() throws Exception {
        String forged = "abc\nfake log line";
        request.addHeader(CorrelationIdFilter.HEADER, forged);

        filter.doFilter(request, response, chain);

        String header = response.getHeader(CorrelationIdFilter.HEADER);
        assertThat(header).isNotEqualTo(forged);
        assertThat(header).as("header is a UUID").matches(UUID_FORMAT);
        assertThat(idDuringRequest.get()).isEqualTo(header);
    }

    @Test
    @DisplayName("too long incoming id → replaced by a new UUID")
    void tooLongIncomingId_isReplaced() throws Exception {
        request.addHeader(CorrelationIdFilter.HEADER, "a".repeat(65));

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(CorrelationIdFilter.HEADER)).hasSize(36); // length of a UUID
    }

    @Test
    @DisplayName("after the request → MDC is empty again, so the next request on this thread starts clean")
    void mdc_isClearedAfterRequest() throws Exception {
        filter.doFilter(request, response, chain);

        assertThat(idDuringRequest.get()).as("set during the request").isNotNull();
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).as("after the request").isNull();
    }

    @Test
    @DisplayName("incoming id with forbidden characters (spaces, quotes, semicolons) → replaced by a new UUID")
    void incomingIdWithForbiddenCharacters_isReplaced() throws Exception {
        String suspicious = "id with \"quotes\"; and spaces";
        request.addHeader(CorrelationIdFilter.HEADER, suspicious);

        filter.doFilter(request, response, chain);

        String header = response.getHeader(CorrelationIdFilter.HEADER);
        assertThat(header).isNotEqualTo(suspicious);
        assertThat(header).as("header is a UUID").matches(UUID_FORMAT);
    }
}

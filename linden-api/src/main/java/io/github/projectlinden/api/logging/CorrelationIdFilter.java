package io.github.projectlinden.api.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Gives every request a correlation ID – in every log line written during the request (MDC field
 * {@value #MDC_KEY}) and in the response header {@value #HEADER} – so a user's "something went wrong at 14:02"
 * can be found in the logs (ENGINEERING.md § 7).
 *
 * <p>Runs before Spring Security, so rejected requests get an ID too. A client may send its own ID, but it is only
 * accepted if it is short and contains nothing but letters, digits and hyphens – anything else could forge log lines.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    /**
     * Request and response header carrying the correlation ID.
     */
    public static final String HEADER = "X-Correlation-Id";

    /**
     * Key in the logging MDC; becomes the field {@code correlationId} in JSON log lines.
     */
    public static final String MDC_KEY = "correlationId";

    private static final Pattern SAFE_ID = Pattern.compile("[A-Za-z0-9-]{8,64}");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String correlationId = correlationIdOf(request);
        MDC.put(MDC_KEY, correlationId);
        response.setHeader(HEADER, correlationId);
        try {
            chain.doFilter(request, response);
        } finally {
            // Threads are reused – the next request must not inherit this ID
            MDC.remove(MDC_KEY);
        }
    }

    private static String correlationIdOf(HttpServletRequest request) {
        String incoming = request.getHeader(HEADER);
        if (incoming != null && SAFE_ID.matcher(incoming).matches()) {
            return incoming;
        }
        return UUID.randomUUID().toString();
    }
}

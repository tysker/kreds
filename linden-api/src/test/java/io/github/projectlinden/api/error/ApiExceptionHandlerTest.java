package io.github.projectlinden.api.error;

import com.jayway.jsonpath.JsonPath;
import io.github.projectlinden.api.logging.CorrelationIdFilter;
import io.github.projectlinden.api.security.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Proves the error contract of ENGINEERING.md § 7: every error is Problem Details with a stable code and the
 * request's correlation ID – and never shows internals.
 */
@WebMvcTest
@Import({SecurityConfig.class, ApiExceptionHandlerTest.FailingController.class})
class ApiExceptionHandlerTest {

    private static final String FAILING_URL = "/test/fail";
    private static final String SECRET = "hunter2";

    /**
     * A test-only endpoint that fails with a message that must never reach the client.
     */
    @RestController
    static class FailingController {

        @GetMapping(FAILING_URL)
        String fail() {
            throw new IllegalStateException("db password is " + SECRET);
        }
    }

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Test
    @DisplayName("unexpected error → 500 problem details with code internal_error, without internals")
    void unexpectedError_returns500ProblemDetail_withoutInternals() throws Exception {
        // when
        MockHttpServletResponse response = mockMvc.perform(get(FAILING_URL).with(jwt()))
                // then
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("internal_error"))
                .andReturn().getResponse();

        assertThat(response.getContentAsString()).as("response body").doesNotContain(SECRET);
    }

    @Test
    @DisplayName("unexpected error → correlation id in the body equals the X-Correlation-Id header")
    void unexpectedError_correlationIdInBody_matchesHeader() throws Exception {
        // when
        MockHttpServletResponse response = mockMvc.perform(get(FAILING_URL).with(jwt()))
                .andReturn().getResponse();

        // then
        String header = response.getHeader(CorrelationIdFilter.HEADER);
        String inBody = JsonPath.read(response.getContentAsString(), "$.correlationId");
        assertThat(header).as("X-Correlation-Id header").isNotBlank();
        assertThat(inBody).as("correlationId in the body").isEqualTo(header);
    }

    @Test
    @DisplayName("unknown path (authenticated) → 404 problem details with code not_found and a correlation id")
    void unknownPath_returns404ProblemDetail_withCodeAndCorrelationId() throws Exception {
        // when + then
        mockMvc.perform(get("/api/v1/does-not-exist").with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("not_found"))
                .andExpect(jsonPath("$.correlationId").isNotEmpty())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("static resource"))));
    }
}

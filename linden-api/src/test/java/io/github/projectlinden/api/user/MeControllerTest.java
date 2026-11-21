package io.github.projectlinden.api.user;

import io.github.projectlinden.api.security.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import(SecurityConfig.class)
class MeControllerTest {

    private static final String ME_URL = "/api/v1/me";

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Test
    @DisplayName("no token → 401")
    void me_withoutToken_returns401() throws Exception {
        // when + then
        mockMvc.perform(get(ME_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("valid token → 200 with the user id from the token's sub claim")
    void me_withValidToken_returnsUserIdFromSubject() throws Exception {
        // given
        String userId = "user-123";

        // when + then
        mockMvc.perform(get(ME_URL).with(jwt().jwt(token -> token.subject(userId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId));
    }

    @Test
    @DisplayName("no token → no session is created (stateless API)")
    void me_withoutToken_createsNoSession() throws Exception {
        // when + then
        mockMvc.perform(get(ME_URL))
                .andExpect(result -> assertThat(result.getRequest().getSession(false)).isNull());
    }
}

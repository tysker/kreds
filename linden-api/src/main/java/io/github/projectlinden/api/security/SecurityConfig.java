package io.github.projectlinden.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security rules for the Linden API.
 *
 * <ul>
 *   <li><b>Deny by default:</b> every request needs a valid Keycloak JWT. Endpoints are opened explicitly, never by accident.</li>
 *   <li><b>Stateless:</b> no sessions and no cookies. Every request brings its own token.</li>
 *   <li><b>No CSRF protection needed:</b> CSRF abuses cookies the browser sends automatically. Without cookies, there is nothing to abuse.</li>
 * </ul>
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(requests -> requests.anyRequest().authenticated())
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable());
        return http.build();
    }
}

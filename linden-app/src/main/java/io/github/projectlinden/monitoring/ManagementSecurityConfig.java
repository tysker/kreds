package io.github.projectlinden.monitoring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security rules for the Actuator endpoints (ADR-0003).
 *
 * <p>Actuator runs on its own management port, which is never reachable from outside – on a server only
 * Prometheus reaches it over the internal Docker network. Therefore health and metrics need no token.
 * Everything else under {@code /actuator} is denied, even if it is exposed by mistake later.
 */
@Configuration
public class ManagementSecurityConfig {

    /**
     * Runs before the API's filter chain, so {@code /actuator/**} is never handled by the JWT rules.
     */
    @Bean
    @Order(1)
    SecurityFilterChain managementSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/actuator/**")
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/prometheus").permitAll()
                        .anyRequest().denyAll())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable());
        return http.build();
    }
}

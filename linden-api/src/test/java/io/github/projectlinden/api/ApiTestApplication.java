package io.github.projectlinden.api;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot configuration for the slice tests of linden-api.
 * The real application class lives in linden-app; this one exists only so @WebMvcTest has a starting point.
 */
@SpringBootApplication
class ApiTestApplication {
}
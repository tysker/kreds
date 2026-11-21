package io.github.projectlinden.monitoring;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves that DEBUG can be switched on deliberately for one package. On a server this is the environment variable
 * {@code LOGGING_LEVEL_IO_GITHUB_PROJECTLINDEN=DEBUG} – Spring Boot maps it to the property used here.
 */
@SpringBootTest(properties = "logging.level.io.github.projectlinden=DEBUG")
class DebugOverrideIT {

    @Test
    @DisplayName("logging.level.io.github.projectlinden=DEBUG → Linden code logs DEBUG, libraries stay at INFO")
    void debugOverride_appliesToLindenOnly() {
        assertThat(LoggerFactory.getLogger("io.github.projectlinden").isDebugEnabled()).as("linden DEBUG").isTrue();
        assertThat(LoggerFactory.getLogger("org.springframework.security").isDebugEnabled())
                .as("spring security DEBUG").isFalse();
    }
}

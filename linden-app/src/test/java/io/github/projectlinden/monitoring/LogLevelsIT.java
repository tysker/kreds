package io.github.projectlinden.monitoring;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the default log levels (ENGINEERING.md § 7): INFO for Linden and for the libraries – DEBUG and TRACE only
 * when switched on deliberately, because that is where request details end up.
 */
@SpringBootTest
class LogLevelsIT {

    @Test
    @DisplayName("Linden code logs INFO and above by default – no DEBUG")
    void lindenCode_logsInfoByDefault() {
        var logger = LoggerFactory.getLogger("io.github.projectlinden");

        assertThat(logger.isInfoEnabled()).as("INFO enabled").isTrue();
        assertThat(logger.isDebugEnabled()).as("DEBUG enabled").isFalse();
    }

    @Test
    @DisplayName("spring security does not log request details (no DEBUG or TRACE) by default")
    void springSecurity_logsNoRequestDetailsByDefault() {
        var logger = LoggerFactory.getLogger("org.springframework.security");

        assertThat(logger.isDebugEnabled()).as("DEBUG enabled").isFalse();
        assertThat(logger.isTraceEnabled()).as("TRACE enabled").isFalse();
    }
}

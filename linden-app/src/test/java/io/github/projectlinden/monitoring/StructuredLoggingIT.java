package io.github.projectlinden.monitoring;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "logging.structured.format.console=logstash")
@ExtendWith(OutputCaptureExtension.class)
class StructuredLoggingIT {

    private static final Logger logger = LoggerFactory.getLogger(StructuredLoggingIT.class);

    @Test
    void jsonLogLine_containsLevelMessageAndTimestamp(CapturedOutput output) {
        // given
        String message = "structured logging contract";

        // when
        logger.info(message);

        // then
        String line = output.getOut().lines()
                .filter(l -> l.contains(message))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no log line with: " + message));
        JsonNode node = new ObjectMapper().readTree(line);

        assertThat(node.path("level").asString()).isEqualTo("INFO");
        assertThat(node.path("message").asString()).isEqualTo(message);
        assertThat(node.has("@timestamp")).as("@timestamp field").isTrue();
    }

}

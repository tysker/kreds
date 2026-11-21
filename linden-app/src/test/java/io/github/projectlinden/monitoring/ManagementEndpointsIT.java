package io.github.projectlinden.monitoring;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the monitoring rules of ADR-0003: Actuator runs only on the management port, exposes only health and
 * Prometheus metrics, and never shows up on the public API port.
 */
@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        // Port 0 = a free random port, so the test never clashes with a running app
        properties = "management.server.port=0")
class ManagementEndpointsIT {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @LocalServerPort
    private int apiPort;

    @LocalManagementPort
    private int managementPort;

    @Test
    @DisplayName("health on the management port → 200 UP, without a token")
    void health_onManagementPort_isUpWithoutToken() throws Exception {
        HttpResponse<String> response = get(managementPort, "/actuator/health");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"UP\"");
    }

    @Test
    @DisplayName("prometheus on the management port → 200 with JVM metrics")
    void prometheus_onManagementPort_returnsMetrics() throws Exception {
        HttpResponse<String> response = get(managementPort, "/actuator/prometheus");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("jvm_memory_used_bytes");
    }

    @Test
    @DisplayName("other actuator endpoints (env can contain secrets) → closed")
    void otherActuatorEndpoints_areClosed() throws Exception {
        HttpResponse<String> response = get(managementPort, "/actuator/env");

        // 401, 403 or 404 depends on Spring internals – every 4xx means "closed"
        assertThat(response.statusCode()).isBetween(400, 499);
    }

    @Test
    @DisplayName("actuator on the public API port → no health answer, it is not served there")
    void actuator_onApiPort_isNotServed() throws Exception {
        HttpResponse<String> response = get(apiPort, "/actuator/health");

        // Usually 404; the API's security rules may turn the error into 401 – both mean "not here"
        assertThat(response.statusCode()).isBetween(400, 499);
        assertThat(response.body()).doesNotContain("UP");
    }

    private HttpResponse<String> get(int port, String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).GET().build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}


package io.github.pangzixiang.whatsit.vertx.core.model;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * The type Health status.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HealthStatus {
    private boolean isHealthy;
    private JsonObject info;
    private LocalDateTime lastUpdated;

    /**
     * Succeed health status.
     *
     * @param info        the info
     * @param lastUpdated the last updated
     * @return the health status
     */
    public static HealthStatus succeed(JsonObject info, LocalDateTime lastUpdated) {
        return HealthStatus
                .builder()
                .isHealthy(true)
                .info(info)
                .lastUpdated(lastUpdated)
                .build();
    }

    /**
     * Succeed health status.
     *
     * @return the health status
     */
    public static HealthStatus succeed() {
        return succeed(null, LocalDateTime.now());
    }

    /**
     * Succeed health status.
     *
     * @param lastUpdated the last updated
     * @return the health status
     */
    public static HealthStatus succeed(LocalDateTime lastUpdated) {
        return succeed(null, lastUpdated);
    }

    /**
     * Fail health status.
     *
     * @param info        the info
     * @param lastUpdated the last updated
     * @return the health status
     */
    public static HealthStatus fail(JsonObject info, LocalDateTime lastUpdated) {
        return HealthStatus
                .builder()
                .isHealthy(false)
                .info(info)
                .lastUpdated(lastUpdated)
                .build();
    }

    /**
     * Fail health status.
     *
     * @return the health status
     */
    public static HealthStatus fail() {
        return fail(null, LocalDateTime.now());
    }

    /**
     * Fail health status.
     *
     * @param lastUpdated the last updated
     * @return the health status
     */
    public static HealthStatus fail(LocalDateTime lastUpdated) {
        return fail(null, lastUpdated);
    }
}

package com.dp_ua.iksparser.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Configuration properties for notification system.
 */
@Configuration
@ConfigurationProperties(prefix = "notification")
@Data
public class NotificationConfigProperties {

    private NotificationMode mode = NotificationMode.BOTH;

    private Boolean onlyForAdmin = true;

    private AggregatedConfig aggregated = new AggregatedConfig();

    private ProcessorConfig processor = new ProcessorConfig();

    @Data
    public static class AggregatedConfig {

        private boolean enabled = true;

        private int cleanupAfterDays = 7;

        private int minutesThreshold = 30;

    }

    @Data
    public static class ProcessorConfig {

        private String cronPattern = "0 15,45 6,10,14,18,22 * * *";

    }

    public boolean isLegacyEnabled() {
        return mode == NotificationMode.LEGACY || mode == NotificationMode.BOTH;
    }

    public boolean isAggregatedEnabled() {
        return (mode == NotificationMode.AGGREGATED || mode == NotificationMode.BOTH)
                && aggregated.isEnabled();
    }

}

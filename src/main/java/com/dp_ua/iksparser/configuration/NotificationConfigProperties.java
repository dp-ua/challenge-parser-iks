package com.dp_ua.iksparser.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties for notification system.
 */
@Configuration
@ConfigurationProperties(prefix = "notification")
@Data
public class NotificationConfigProperties {

    /**
     * Notification mode: LEGACY, AGGREGATED, or BOTH.
     */
    private NotificationMode mode = NotificationMode.BOTH;

    /**
     * Aggregated notification settings.
     */
    private AggregatedConfig aggregated = new AggregatedConfig();

    @Getter
    @Setter
    public static class AggregatedConfig {

        /**
         * Whether aggregated notifications are enabled.
         */
        private boolean enabled = true;

        /**
         * Maximum number of notifications to include in a single message.
         */
        private int maxNotificationsPerMessage = 50;

        /**
         * Number of days after which processed notifications are deleted.
         */
        private int cleanupAfterDays = 7;

    }

    /**
     * Notification processor configuration.
     */
    private ProcessorConfig processor = new ProcessorConfig();

    @Getter
    @Setter
    public static class ProcessorConfig {

        /**
         * Cron pattern for notification processor.
         */
        private String cronPattern = "0 15,45 6,10,14,18,22 * * *";

    }

    public boolean isLegacyEnabled() {
        return mode == NotificationMode.LEGACY || mode == NotificationMode.BOTH;
    }

    public boolean isAggregatedEnabled() {
        return (mode == NotificationMode.AGGREGATED || mode == NotificationMode.BOTH)
                && aggregated.isEnabled();
    }

    public int getMaxNotificationsPerMessage() {
        return aggregated.getMaxNotificationsPerMessage();
    }

    public int getCleanupAfterDays() {
        return aggregated.getCleanupAfterDays();
    }

}

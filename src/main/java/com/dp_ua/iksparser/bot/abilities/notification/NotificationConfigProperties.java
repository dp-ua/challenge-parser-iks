package com.dp_ua.iksparser.bot.abilities.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Configuration properties for notification system
 */
@Configuration
@ConfigurationProperties(prefix = "notification")
@Data
public class NotificationConfigProperties {

    /**
     * Notification mode: LEGACY, AGGREGATED, or BOTH
     */
    private NotificationMode mode = NotificationMode.BOTH;

    /**
     * Aggregated notification settings
     */
    private AggregatedSettings aggregated = new AggregatedSettings();

    @Data
    public static class AggregatedSettings {

        /**
         * Enable aggregated notifications
         */
        private boolean enabled = true;

        /**
         * Scheduler interval in milliseconds
         */
        private long schedulerInterval = 600000; // 10 minutes

        /**
         * Maximum notifications per user in one message
         */
        private int maxNotificationsPerMessage = 50;

        /**
         * Delete old processed notifications after N days
         */
        private int cleanupAfterDays = 7;

    }

    /**
     * Check if legacy mode is enabled
     */
    public boolean isLegacyEnabled() {
        return mode == NotificationMode.LEGACY || mode == NotificationMode.BOTH;
    }

    /**
     * Check if aggregated mode is enabled
     */
    public boolean isAggregatedEnabled() {
        return (mode == NotificationMode.AGGREGATED || mode == NotificationMode.BOTH)
                && aggregated.isEnabled();
    }

}

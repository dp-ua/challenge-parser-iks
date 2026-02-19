package com.dp_ua.iksparser.bot.abilities.notification;

/**
 * Notification mode for controlling how notifications are sent
 */
public enum NotificationMode {
    /**
     * Legacy mode - send notifications immediately per athlete
     */
    LEGACY,

    /**
     * Aggregated mode - collect notifications and send aggregated messages
     */
    AGGREGATED,

    /**
     * Both modes - run both systems in parallel for testing
     */
    BOTH
}

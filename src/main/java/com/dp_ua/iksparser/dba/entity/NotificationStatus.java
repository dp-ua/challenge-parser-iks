package com.dp_ua.iksparser.dba.entity;

/**
 * Status of notification in the queue
 */
public enum NotificationStatus {
    /**
     * Notification is created and waiting for processing
     */
    NEW,

    /**
     * Notification is currently being processed
     */
    PROCESSING,

    /**
     * Notification was successfully sent
     */
    SENT,

    /**
     * Notification processing failed
     */
    ERROR,

    /**
     * Notification was cancelled (e.g., subscriber unsubscribed)
     */
    CANCELLED
}

package com.dp_ua.iksparser.service.cron;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.dp_ua.iksparser.bot.abilities.notification.NotificationProcessorFacade;
import com.dp_ua.iksparser.configuration.NotificationConfigProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduler for notification processing.
 * Only triggers the facade, no business logic here.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProcessor {

    private final NotificationProcessorFacade facade;
    private final NotificationConfigProperties config;

    /**
     * Process pending notifications every 25 minutes
     */
    @Scheduled(cron = "0 0/25 10-23 * * *")
    public void processPendingNotifications() {
        log.info("Scheduled notification processing triggered");
        facade.processAllPendingNotifications();
    }

    /**
     * Reset stuck notifications every hour
     */
    @Scheduled(cron = "0 0 * * * *")
    public void resetStuckNotifications() {
        log.info("Scheduled stuck notifications reset triggered");
        facade.resetStuckNotifications(
                config.getAggregated().getMinutesThreshold()
        );
    }

    /**
     * Cleanup old notifications daily at 3 AM
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupOldNotifications() {
        log.info("Scheduled cleanup triggered");
        facade.cleanupOldNotifications(
                config.getAggregated().getCleanupAfterDays()
        );
    }

}

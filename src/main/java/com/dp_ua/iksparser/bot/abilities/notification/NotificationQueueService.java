package com.dp_ua.iksparser.bot.abilities.notification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.entity.NotificationQueueEntity;
import com.dp_ua.iksparser.dba.entity.NotificationStatus;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import com.dp_ua.iksparser.dba.repo.NotificationQueueRepository;
import com.dp_ua.iksparser.dba.service.SubscriberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for working with notification queue
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationQueueService {

    private final NotificationQueueRepository notificationQueueRepository;
    private final SubscriberService subscriberService;

    @Transactional
    public void saveNotificationsToQueue(ParticipantEntity participant, List<HeatLineEntity> heatLines) {
        if (heatLines.isEmpty()) {
            log.debug("No heat lines for participant '{}', skipping notification queue", participant.getId());
            return;
        }

        log.info("Saving notifications to queue for participant '{}' with '{}' heat lines",
                participant.getId(), heatLines.size());

        subscriberService.findAllByParticipant(participant).forEach(subscriber -> {
            var chatId = subscriber.getChatId();

            heatLines.forEach(heatLine -> {
                NotificationQueueEntity notification = NotificationQueueEntity.builder()
                        .chatId(chatId)
                        .participant(participant)
                        .heatLine(heatLine)
                        .build();

                notificationQueueRepository.save(notification);
                log.debug("Added notification to queue: '{}'", notification);
            });
        });
    }

    @Transactional
    public int markAsProcessing(String chatId) {
        var updatedCount = notificationQueueRepository.updateStatusForChat(
                chatId,
                NotificationStatus.NEW,
                NotificationStatus.PROCESSING,
                LocalDateTime.now()
        );
        log.debug("Marked {} notifications as PROCESSING for chatId '{}'", updatedCount, chatId);
        return updatedCount;
    }

    @Transactional(readOnly = true)
    public List<NotificationQueueEntity> getProcessingNotifications(String chatId) {
        return notificationQueueRepository.findByChatIdAndStatusWithDetails(
                chatId,
                NotificationStatus.PROCESSING
        );
    }

    @Transactional
    public void markAsSent(List<Long> notificationIds) {
        if (notificationIds.isEmpty()) return;

        var now = LocalDateTime.now();
        var updatedCount = notificationQueueRepository.updateStatusBatch(
                notificationIds,
                NotificationStatus.SENT,
                now,
                now
        );
        log.info("Marked {} notifications as SENT", updatedCount);
    }

    @Transactional
    public void markAsError(List<Long> notificationIds, String errorMessage) {
        if (notificationIds.isEmpty()) return;

        var now = LocalDateTime.now();
        var updatedCount = notificationQueueRepository.updateStatusWithError(
                notificationIds,
                NotificationStatus.ERROR,
                now,
                errorMessage,
                now
        );
        log.warn("Marked {} notifications as ERROR: {}", updatedCount, errorMessage);
    }

    @Transactional
    public void resetStatus(List<Long> notificationIds, NotificationStatus newStatus) {
        if (notificationIds.isEmpty()) return;

        var updatedCount = notificationQueueRepository.resetStatusBatch(
                notificationIds,
                newStatus,
                LocalDateTime.now()
        );
        log.info("Reset {} notifications to status {}", updatedCount, newStatus);
    }

    public Map<ParticipantEntity, List<NotificationQueueEntity>> groupByParticipant(
            List<NotificationQueueEntity> notifications) {
        return notifications.stream()
                .collect(Collectors.groupingBy(NotificationQueueEntity::getParticipant));
    }

}

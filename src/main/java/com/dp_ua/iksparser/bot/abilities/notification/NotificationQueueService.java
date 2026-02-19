package com.dp_ua.iksparser.bot.abilities.notification;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.entity.NotificationQueueEntity;
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

    /**
     * Save notifications to queue for a participant and their heatLines
     *
     * @param participant The participant
     * @param heatLines   List of heat lines
     */
    @Transactional
    public void saveNotificationsToQueue(ParticipantEntity participant, List<HeatLineEntity> heatLines) {
        if (heatLines.isEmpty()) {
            log.debug("No heat lines for participant '{}', skipping notification queue", participant.getId());
            return;
        }

        log.info("Saving notifications to queue for participant '{}' with '{}' heat lines",
                participant.getId(), heatLines.size());

        // Get all subscribers for this participant
        subscriberService.findAllByParticipant(participant).forEach(subscriber -> {
            String chatId = subscriber.getChatId();

            // Add each heat line to notification queue
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

}

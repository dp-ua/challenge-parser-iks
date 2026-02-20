package com.dp_ua.iksparser.bot.abilities.notification;

import static com.dp_ua.iksparser.bot.Icon.COMPETITION;
import static com.dp_ua.iksparser.bot.Icon.ENOUGH;
import static com.dp_ua.iksparser.service.MessageCreator.SERVICE;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import com.dp_ua.iksparser.bot.command.impl.CommandDeleteMessage;
import com.dp_ua.iksparser.bot.command.impl.competition.CommandCompetition;
import com.dp_ua.iksparser.configuration.NotificationConfigProperties;
import com.dp_ua.iksparser.configuration.TelegramBotProperties;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.entity.NotificationQueueEntity;
import com.dp_ua.iksparser.dba.entity.NotificationStatus;
import com.dp_ua.iksparser.dba.repo.NotificationQueueRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Facade for processing notification queue.
 * Orchestrates the entire notification processing flow.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationProcessorFacade {

    private final NotificationQueueService notificationQueueService;
    private final NotificationQueueRepository notificationQueueRepository;
    private final NotificationMessageBuilder messageBuilder;
    private final ApplicationEventPublisher eventPublisher;
    private final TelegramBotProperties telegramBotProperties;
    private final NotificationConfigProperties notationConfigProperties;

    private static final int MAX_RETRY_COUNT = 3;

    @Transactional
    public void processAllPendingNotifications() {
        log.info("Starting notification processing...");

        try {
            var chatIds = notificationQueueRepository.findDistinctChatIdsByStatus(NotificationStatus.NEW);
            log.info("Found {} chats with pending notifications", chatIds.size());
            var filteredChatIds = filterChatsIfNeeded(chatIds);

            filteredChatIds.forEach(this::processChatNotifications);

        } catch (Exception e) {
            log.error("Error in notification processing", e);
        }
    }

    private List<String> filterChatsIfNeeded(List<String> chatIds) {
        if (Boolean.TRUE.equals(notationConfigProperties.getOnlyForAdmin())) {
            var filtered = chatIds.stream()
                    .filter(telegramBotProperties::isAdmin)
                    .toList();
            log.info("Notifications only for admin chats: {} chats after filtering", filtered.size());
            return filtered;
        }
        return chatIds;
    }

    @Transactional
    public void processChatNotifications(String chatId) {
        try {
            log.debug("Processing notifications for chatId '{}'", chatId);

            var marked = notificationQueueService.markAsProcessing(chatId);
            if (marked == 0) {
                log.debug("No notifications to process for chatId '{}'", chatId);
                return;
            }

            var notifications = notificationQueueService.getProcessingNotifications(chatId);

            if (notifications.isEmpty()) {
                log.warn("No PROCESSING notifications found for chatId '{}', possible race condition", chatId);
                return;
            }

            var grouped = notificationQueueService.groupByParticipant(notifications);

            var messages = messageBuilder.buildAggregatedMessages(grouped);

            log.info("Built {} messages for chatId '{}'", messages.size(), chatId);

            var allSent = true;
            for (var message : messages) {
                var keyboard = buildNotificationKeyboard(message.getCompetition());

                var sent = sendNotification(chatId, message.getText(), keyboard);

                if (sent) {
                    notificationQueueService.markAsSent(message.getNotificationIds());
                    log.debug("Sent message about competition '{}' with {} notifications",
                            message.getCompetition().getName(),
                            message.getNotificationIds().size());
                } else {
                    notificationQueueService.markAsError(
                            message.getNotificationIds(),
                            "Failed to send telegram message"
                    );
                    allSent = false;
                }

                if (messages.size() > 1) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("Interrupted while waiting between messages");
                        break;
                    }
                }
            }

            if (allSent) {
                log.info("Successfully sent all {} messages to chatId '{}'",
                        messages.size(), chatId);
            } else {
                log.warn("Some messages failed to send to chatId '{}'", chatId);
            }

        } catch (Exception e) {
            log.error("Error processing notifications for chatId '{}'", chatId, e);
        }
    }

    @Transactional
    public void retryFailedNotifications() {
        var failedNotifications = notificationQueueRepository.findByStatus(NotificationStatus.ERROR)
                .stream()
                .filter(n -> n.getRetryCount() < MAX_RETRY_COUNT)
                .toList();

        if (failedNotifications.isEmpty()) {
            return;
        }

        log.info("Retrying {} failed notifications", failedNotifications.size());

        var ids = failedNotifications.stream()
                .map(NotificationQueueEntity::getId)
                .toList();

        notificationQueueService.resetStatus(ids, NotificationStatus.NEW);
    }

    @Transactional
    public void resetStuckNotifications(int minutesThreshold) {
        var threshold = LocalDateTime.now().minusMinutes(minutesThreshold);

        var stuckNotifications =
                notificationQueueRepository.findByStatus(NotificationStatus.PROCESSING)
                        .stream()
                        .filter(n -> n.getCreated().isBefore(threshold))
                        .toList();

        if (!stuckNotifications.isEmpty()) {
            log.warn("Found {} stuck notifications, resetting to NEW",
                    stuckNotifications.size());

            var ids = stuckNotifications.stream()
                    .map(NotificationQueueEntity::getId)
                    .toList();

            notificationQueueService.resetStatus(ids, NotificationStatus.NEW);
        }
    }

    @Transactional
    public void cleanupOldNotifications(int daysOld) {
        var threshold = LocalDateTime.now().minusDays(daysOld);
        var deleted = notificationQueueRepository.deleteByProcessedAtBefore(threshold);

        if (deleted > 0) {
            log.info("Cleaned up {} old notifications", deleted);
        }
    }

    private InlineKeyboardMarkup buildNotificationKeyboard(CompetitionEntity competition) {
        var keyboard = new InlineKeyboardMarkup();
        var rows = new ArrayList<List<InlineKeyboardButton>>();

        var row1 = new ArrayList<InlineKeyboardButton>();
        var competitionButton = SERVICE.getKeyboardButton(
                COMPETITION + " Переглянути змагання",
                CommandCompetition.getCallbackCommand(competition.getId())
        );
        row1.add(competitionButton);
        rows.add(row1);

        var row2 = new ArrayList<InlineKeyboardButton>();
        var hideButton = SERVICE.getKeyboardButton(
                "Сховати" + ENOUGH,
                "/" + CommandDeleteMessage.command
        );
        row2.add(hideButton);
        rows.add(row2);

        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private boolean sendNotification(String chatId, String messageText, InlineKeyboardMarkup keyboard) {
        try {
            var event = SERVICE.getSendMessageEvent(chatId, messageText, keyboard, null);
            eventPublisher.publishEvent(event);
            return true;
        } catch (Exception e) {
            log.error("Failed to publish send message event for chatId '{}'", chatId, e);
            return false;
        }
    }

}

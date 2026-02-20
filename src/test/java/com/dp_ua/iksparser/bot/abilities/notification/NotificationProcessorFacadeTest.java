package com.dp_ua.iksparser.bot.abilities.notification;

import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.createTestCompetition;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.createTestEvent;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.createTestHeat;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.createTestHeatLine;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.createTestNotification;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.createTestParticipant;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.dp_ua.iksparser.configuration.NotificationConfigProperties;
import com.dp_ua.iksparser.configuration.TelegramBotProperties;
import com.dp_ua.iksparser.dba.entity.DayEntity;
import com.dp_ua.iksparser.dba.repo.NotificationQueueRepository;

@ExtendWith(MockitoExtension.class)
class NotificationProcessorFacadeTest {

    public static final long NOTIFICATION_ID = 1L;
    @Mock
    NotificationQueueService notificationQueueService;
    @Mock
    NotificationQueueRepository notificationQueueRepository;
    @Mock
    NotificationMessageBuilder messageBuilder;
    @Mock
    ApplicationEventPublisher eventPublisher;
    @Mock
    TelegramBotProperties telegramBotProperties;
    @Mock
    NotificationConfigProperties notationConfigProperties;

    @InjectMocks
    NotificationProcessorFacade facade;

    @Test
    void processChatNotifications_shouldSendMessagesAndMarkAsSent() {
        String chatId = "123";
        var competition = createTestCompetition();
        var day = new DayEntity("15.12.23", "151223", "День 1", "Day 1");
        day.setCompetition(competition);
        competition.addDay(day);

        var event = createTestEvent();
        event.setDay(day);
        day.addEvent(event);

        var heat = createTestHeat();
        heat.setEvent(event);
        event.addHeat(heat);

        var participant = createTestParticipant();

        var heatLine = createTestHeatLine(participant, heat);
        heatLine.setHeat(heat);
        heatLine.setParticipant(participant);
        heat.addHeatLine(heatLine);

        var notification = createTestNotification(chatId, participant, heatLine);
        notification.setId(NOTIFICATION_ID);

        when(notificationQueueService.markAsProcessing(chatId)).thenReturn(1);
        when(notificationQueueService.getProcessingNotifications(chatId)).thenReturn(List.of(notification));

        var message = NotificationMessage.builder()
                .competition(competition)
                .text("test message")
                .notificationIds(List.of(NOTIFICATION_ID))
                .build();

        when(messageBuilder.buildAggregatedMessages(any())).thenReturn(List.of(message));

        facade.processChatNotifications(chatId);

        verify(notificationQueueService).markAsProcessing(chatId);
        verify(notificationQueueService).getProcessingNotifications(chatId);
        verify(messageBuilder).buildAggregatedMessages(any());
        verify(notificationQueueService).markAsSent(List.of(NOTIFICATION_ID));
    }

    @Test
    void processChatNotifications_shouldNotSendIfNoNotifications() {
        String chatId = "123";
        when(notificationQueueService.markAsProcessing(chatId)).thenReturn(0);

        facade.processChatNotifications(chatId);

        verify(notificationQueueService).markAsProcessing(chatId);
        verify(notificationQueueService, never()).getProcessingNotifications(chatId);
        verify(messageBuilder, never()).buildAggregatedMessages(any());
    }

}

package com.dp_ua.iksparser.bot.abilities.notification;

import static com.dp_ua.iksparser.bot.abilities.notification.NotificationQueueServiceTestUtils.EXPECTED_NOTIFICATIONS_1_SUB_1_HEAT;
import static com.dp_ua.iksparser.bot.abilities.notification.NotificationQueueServiceTestUtils.EXPECTED_NOTIFICATIONS_1_SUB_2_HEATS;
import static com.dp_ua.iksparser.bot.abilities.notification.NotificationQueueServiceTestUtils.EXPECTED_NOTIFICATIONS_2_SUBS_1_HEAT;
import static com.dp_ua.iksparser.bot.abilities.notification.NotificationQueueServiceTestUtils.EXPECTED_NOTIFICATIONS_2_SUBS_2_HEATS;
import static com.dp_ua.iksparser.bot.abilities.notification.NotificationQueueServiceTestUtils.TEST_CHAT_ID_1;
import static com.dp_ua.iksparser.bot.abilities.notification.NotificationQueueServiceTestUtils.TEST_CHAT_ID_2;
import static com.dp_ua.iksparser.bot.abilities.notification.NotificationQueueServiceTestUtils.TEST_CHAT_ID_3;
import static com.dp_ua.iksparser.bot.abilities.notification.NotificationQueueServiceTestUtils.TEST_HEAT_LINE_ID_1;
import static com.dp_ua.iksparser.bot.abilities.notification.NotificationQueueServiceTestUtils.TEST_HEAT_LINE_ID_2;
import static com.dp_ua.iksparser.bot.abilities.notification.NotificationQueueServiceTestUtils.TEST_HEAT_LINE_ID_3;
import static com.dp_ua.iksparser.bot.abilities.notification.NotificationQueueServiceTestUtils.TEST_PARTICIPANT_ID_1;
import static com.dp_ua.iksparser.bot.abilities.notification.NotificationQueueServiceTestUtils.THREE_HEAT_LINES;
import static com.dp_ua.iksparser.bot.abilities.notification.NotificationQueueServiceTestUtils.THREE_SUBSCRIBERS;
import static com.dp_ua.iksparser.bot.abilities.notification.NotificationQueueServiceTestUtils.TWO_HEAT_LINES;
import static com.dp_ua.iksparser.bot.abilities.notification.NotificationQueueServiceTestUtils.TWO_SUBSCRIBERS;
import static com.dp_ua.iksparser.bot.abilities.notification.NotificationQueueServiceTestUtils.createTestHeatLine;
import static com.dp_ua.iksparser.bot.abilities.notification.NotificationQueueServiceTestUtils.createTestHeatLines;
import static com.dp_ua.iksparser.bot.abilities.notification.NotificationQueueServiceTestUtils.createTestParticipant;
import static com.dp_ua.iksparser.bot.abilities.notification.NotificationQueueServiceTestUtils.createTestSubscriber;
import static com.dp_ua.iksparser.bot.abilities.notification.NotificationQueueServiceTestUtils.createTestSubscribers;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.entity.NotificationQueueEntity;
import com.dp_ua.iksparser.dba.repo.NotificationQueueRepository;
import com.dp_ua.iksparser.dba.service.SubscriberService;

@ExtendWith(MockitoExtension.class)
class NotificationQueueServiceTest {

    @Mock
    private NotificationQueueRepository notificationQueueRepository;

    @Mock
    private SubscriberService subscriberService;

    @InjectMocks
    private NotificationQueueService notificationQueueService;

    @Test
    void saveNotificationsToQueue_whenHeatLinesEmpty_shouldNotSaveAnything() {
        // Given
        var participant = createTestParticipant(TEST_PARTICIPANT_ID_1);
        var emptyHeatLines = Collections.<HeatLineEntity>emptyList();

        // When
        notificationQueueService.saveNotificationsToQueue(participant, emptyHeatLines);

        // Then
        verify(subscriberService, never()).findAllByParticipant(any());
        verify(notificationQueueRepository, never()).save(any());
    }

    @Test
    void saveNotificationsToQueue_whenNoSubscribers_shouldNotSaveAnything() {
        // Given
        var participant = createTestParticipant(TEST_PARTICIPANT_ID_1);
        var heatLines = createTestHeatLines(TEST_HEAT_LINE_ID_1);

        when(subscriberService.findAllByParticipant(participant))
                .thenReturn(Collections.emptyList());

        // When
        notificationQueueService.saveNotificationsToQueue(participant, heatLines);

        // Then
        verify(subscriberService).findAllByParticipant(participant);
        verify(notificationQueueRepository, never()).save(any());
    }

    @Test
    void saveNotificationsToQueue_whenOneSubscriberAndOneHeatLine_shouldSaveOneNotification() {
        // Given
        var participant = createTestParticipant(TEST_PARTICIPANT_ID_1);
        var heatLine = createTestHeatLine(TEST_HEAT_LINE_ID_1);
        var heatLines = List.of(heatLine);

        var subscriber = createTestSubscriber(TEST_CHAT_ID_1);
        var subscribers = List.of(subscriber);

        when(subscriberService.findAllByParticipant(participant))
                .thenReturn(subscribers);

        // When
        notificationQueueService.saveNotificationsToQueue(participant, heatLines);

        // Then
        var captor = ArgumentCaptor.forClass(NotificationQueueEntity.class);
        verify(notificationQueueRepository, times(EXPECTED_NOTIFICATIONS_1_SUB_1_HEAT))
                .save(captor.capture());

        var saved = captor.getValue();
        assertThat(saved.getChatId()).isEqualTo(TEST_CHAT_ID_1);
        assertThat(saved.getParticipant()).isEqualTo(participant);
        assertThat(saved.getHeatLine()).isEqualTo(heatLine);
    }

    @Test
    void saveNotificationsToQueue_whenOneSubscriberAndMultipleHeatLines_shouldSaveMultipleNotifications() {
        // Given
        var participant = createTestParticipant(TEST_PARTICIPANT_ID_1);
        var heatLine1 = createTestHeatLine(TEST_HEAT_LINE_ID_1);
        var heatLine2 = createTestHeatLine(TEST_HEAT_LINE_ID_2);
        var heatLines = List.of(heatLine1, heatLine2);

        var subscriber = createTestSubscriber(TEST_CHAT_ID_1);
        var subscribers = List.of(subscriber);

        when(subscriberService.findAllByParticipant(participant))
                .thenReturn(subscribers);

        // When
        notificationQueueService.saveNotificationsToQueue(participant, heatLines);

        // Then
        var captor = ArgumentCaptor.forClass(NotificationQueueEntity.class);
        verify(notificationQueueRepository, times(EXPECTED_NOTIFICATIONS_1_SUB_2_HEATS))
                .save(captor.capture());

        var savedNotifications = captor.getAllValues();
        assertThat(savedNotifications).hasSize(TWO_HEAT_LINES);
        assertThat(savedNotifications.get(0).getHeatLine()).isEqualTo(heatLine1);
        assertThat(savedNotifications.get(1).getHeatLine()).isEqualTo(heatLine2);
        assertThat(savedNotifications)
                .allMatch(n -> TEST_CHAT_ID_1.equals(n.getChatId()))
                .allMatch(n -> participant.equals(n.getParticipant()));
    }

    @Test
    void saveNotificationsToQueue_whenMultipleSubscribersAndOneHeatLine_shouldSaveNotificationsForAllSubscribers() {
        // Given
        var participant = createTestParticipant(TEST_PARTICIPANT_ID_1);
        var heatLine = createTestHeatLine(TEST_HEAT_LINE_ID_1);
        var heatLines = List.of(heatLine);

        var subscribers = createTestSubscribers(TEST_CHAT_ID_1, TEST_CHAT_ID_2);

        when(subscriberService.findAllByParticipant(participant))
                .thenReturn(subscribers);

        // When
        notificationQueueService.saveNotificationsToQueue(participant, heatLines);

        // Then
        var captor = ArgumentCaptor.forClass(NotificationQueueEntity.class);
        verify(notificationQueueRepository, times(EXPECTED_NOTIFICATIONS_2_SUBS_1_HEAT))
                .save(captor.capture());

        var savedNotifications = captor.getAllValues();
        assertThat(savedNotifications).hasSize(TWO_SUBSCRIBERS);
        assertThat(savedNotifications)
                .extracting(NotificationQueueEntity::getChatId)
                .containsExactlyInAnyOrder(TEST_CHAT_ID_1, TEST_CHAT_ID_2);
        assertThat(savedNotifications)
                .allMatch(n -> heatLine.equals(n.getHeatLine()))
                .allMatch(n -> participant.equals(n.getParticipant()));
    }

    @Test
    void saveNotificationsToQueue_whenMultipleSubscribersAndMultipleHeatLines_shouldSaveAllCombinations() {
        // Given
        var participant = createTestParticipant(TEST_PARTICIPANT_ID_1);
        var heatLines = createTestHeatLines(TEST_HEAT_LINE_ID_1, TEST_HEAT_LINE_ID_2);

        var subscribers = createTestSubscribers(TEST_CHAT_ID_1, TEST_CHAT_ID_2);

        when(subscriberService.findAllByParticipant(participant))
                .thenReturn(subscribers);

        // When
        notificationQueueService.saveNotificationsToQueue(participant, heatLines);

        // Then
        verify(notificationQueueRepository, times(EXPECTED_NOTIFICATIONS_2_SUBS_2_HEATS))
                .save(any(NotificationQueueEntity.class));
    }

    @Test
    void saveNotificationsToQueue_whenThreeSubscribersAndThreeHeatLines_shouldSaveNineCombinations() {
        // Given
        var participant = createTestParticipant(TEST_PARTICIPANT_ID_1);
        var heatLines = createTestHeatLines(
                TEST_HEAT_LINE_ID_1,
                TEST_HEAT_LINE_ID_2,
                TEST_HEAT_LINE_ID_3
        );

        var subscribers = createTestSubscribers(
                TEST_CHAT_ID_1,
                TEST_CHAT_ID_2,
                TEST_CHAT_ID_3
        );

        when(subscriberService.findAllByParticipant(participant))
                .thenReturn(subscribers);

        // When
        notificationQueueService.saveNotificationsToQueue(participant, heatLines);

        // Then
        var expectedNotifications = THREE_SUBSCRIBERS * THREE_HEAT_LINES;
        verify(notificationQueueRepository, times(expectedNotifications))
                .save(any(NotificationQueueEntity.class));
    }

    @Test
    void saveNotificationsToQueue_shouldPreserveParticipantInAllNotifications() {
        // Given
        var participant = createTestParticipant(TEST_PARTICIPANT_ID_1);
        var heatLines = createTestHeatLines(TEST_HEAT_LINE_ID_1, TEST_HEAT_LINE_ID_2);
        var subscribers = createTestSubscribers(TEST_CHAT_ID_1);

        when(subscriberService.findAllByParticipant(participant))
                .thenReturn(subscribers);

        // When
        notificationQueueService.saveNotificationsToQueue(participant, heatLines);

        // Then
        var captor = ArgumentCaptor.forClass(NotificationQueueEntity.class);
        verify(notificationQueueRepository, times(TWO_HEAT_LINES)).save(captor.capture());

        assertThat(captor.getAllValues())
                .allMatch(n -> participant.equals(n.getParticipant()));
    }

}

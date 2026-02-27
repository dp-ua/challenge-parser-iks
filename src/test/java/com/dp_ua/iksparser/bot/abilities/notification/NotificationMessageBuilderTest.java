package com.dp_ua.iksparser.bot.abilities.notification;

import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.TEST_CHAT_ID_1;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.TEST_COMPETITION_URL;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.TEST_EVENT_NAME;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.TEST_PARTICIPANT_NAME_1;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.TEST_PARTICIPANT_SURNAME_1;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.createDay;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.createTestCompetition;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.createTestEvent;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.createTestHeat;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.createTestHeatLine;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.createTestNotification;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.createTestParticipant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dp_ua.iksparser.bot.abilities.infoview.CompetitionView;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.entity.EventEntity;
import com.dp_ua.iksparser.dba.entity.HeatEntity;
import com.dp_ua.iksparser.dba.entity.NotificationQueueEntity;

@ExtendWith(MockitoExtension.class)
class NotificationMessageBuilderTest {

    @Mock
    CompetitionView competitionView;

    @InjectMocks
    NotificationMessageBuilder builder;

    @Test
    void buildAggregatedMessages_shouldBuildSingleMessageForSingleNewEnrollment() {
        // Arrange: создаём реальные сущности через NotificationTestUtils
        CompetitionEntity competition = createTestCompetition();
        EventEntity event = createTestEvent();
        event.setResultUrl(null); // Нет результатов - это новая заявка

        var day = createDay();
        day.addEvent(event);
        competition.addDay(day);
        event.setDay(day);

        HeatEntity heat = createTestHeat();
        heat.setEvent(event);

        var participant = createTestParticipant();
        var heatLine = createTestHeatLine(participant, heat);

        NotificationQueueEntity notification = createTestNotification(TEST_CHAT_ID_1, participant, heatLine);
        notification.setId(1L);

        when(competitionView.nameAndDate(any())).thenReturn("Чемпіонат України, 15.12.23");
        when(competitionView.area(any())).thenReturn("Київ");

        // Act
        var messages = builder.buildAggregatedMessages(
                Map.of(competition, List.of(notification))
        );

        // Assert
        assertThat(messages).hasSize(1);
        var message = messages.get(0);
        assertThat(message.getCompetition()).isEqualTo(competition);
        assertThat(message.getNotificationIds()).containsExactly(1L);
        assertThat(message.getText())
                .contains("Чемпіонат України")
                .contains(TEST_PARTICIPANT_SURNAME_1 + " " + TEST_PARTICIPANT_NAME_1)
                .contains(TEST_EVENT_NAME)
                .contains(TEST_COMPETITION_URL)
                .contains("Нові заявки:"); // Проверяем заголовок секции
    }

    @Test
    void buildAggregatedMessages_shouldBuildSingleMessageForSingleResult() {
        // Arrange
        CompetitionEntity competition = createTestCompetition();
        EventEntity event = createTestEvent();
        event.setResultUrl("http://example.com/results"); // Есть результаты

        var day = createDay();
        day.addEvent(event);
        competition.addDay(day);
        event.setDay(day);

        HeatEntity heat = createTestHeat();
        heat.setEvent(event);

        var participant = createTestParticipant();
        var heatLine = createTestHeatLine(participant, heat);

        NotificationQueueEntity notification = createTestNotification(TEST_CHAT_ID_1, participant, heatLine);
        notification.setId(1L);

        when(competitionView.nameAndDate(any())).thenReturn("Чемпіонат України, 15.12.23");
        when(competitionView.area(any())).thenReturn("Київ");

        // Act
        var messages = builder.buildAggregatedMessages(
                Map.of(competition, List.of(notification))
        );

        // Assert
        assertThat(messages).hasSize(1);
        var message = messages.get(0);
        assertThat(message.getCompetition()).isEqualTo(competition);
        assertThat(message.getNotificationIds()).containsExactly(1L);
        assertThat(message.getText())
                .contains("Чемпіонат України")
                .contains(TEST_PARTICIPANT_SURNAME_1 + " " + TEST_PARTICIPANT_NAME_1)
                .contains("Є результати:"); // Проверяем заголовок для результатов
    }

    @Test
    void buildAggregatedMessages_shouldBuildSeparateMessagesForEnrollmentsAndResults() {
        // Arrange
        CompetitionEntity competition = createTestCompetition();

        // Событие 1 - новая заявка (без результатов)
        EventEntity event1 = createTestEvent();
        event1.setResultUrl(null);
        event1.setEventName("50м вільний стиль");

        var day = createDay();
        day.addEvent(event1);
        competition.addDay(day);
        event1.setDay(day);

        HeatEntity heat1 = createTestHeat();
        heat1.setEvent(event1);

        var participant1 = createTestParticipant();
        var heatLine1 = createTestHeatLine(participant1, heat1);

        NotificationQueueEntity notification1 = createTestNotification(TEST_CHAT_ID_1, participant1, heatLine1);
        notification1.setId(1L);

        // Событие 2 - результат
        EventEntity event2 = createTestEvent();
        event2.setResultUrl("http://example.com/results");
        event2.setEventName("100м вільний стиль");
        day.addEvent(event2);
        event2.setDay(day);

        HeatEntity heat2 = createTestHeat();
        heat2.setEvent(event2);

        var participant2 = createTestParticipant();
        participant2.setName("Петро");
        participant2.setSurname("Петренко");
        var heatLine2 = createTestHeatLine(participant2, heat2);

        NotificationQueueEntity notification2 = createTestNotification(TEST_CHAT_ID_1, participant2, heatLine2);
        notification2.setId(2L);

        when(competitionView.nameAndDate(any())).thenReturn("Чемпіонат України, 15.12.23");
        when(competitionView.area(any())).thenReturn("Київ");

        // Act
        var messages = builder.buildAggregatedMessages(
                Map.of(competition, List.of(notification1, notification2))
        );

        // Assert
        assertThat(messages).hasSize(2); // Должно быть 2 сообщения: одно для заявок, одно для результатов

        // Первое сообщение - новые заявки
        var enrollmentMessage = messages.get(0);
        assertThat(enrollmentMessage.getText())
                .contains("Нові заявки:")
                .contains("50м вільний стиль")
                .contains(TEST_PARTICIPANT_SURNAME_1 + " " + TEST_PARTICIPANT_NAME_1);
        assertThat(enrollmentMessage.getNotificationIds()).containsExactly(1L);

        // Второе сообщение - результаты
        var resultMessage = messages.get(1);
        assertThat(resultMessage.getText())
                .contains("Є результати:")
                .contains("100м вільний стиль")
                .contains("Петренко Петро");
        assertThat(resultMessage.getNotificationIds()).containsExactly(2L);
    }

    @Test
    void buildAggregatedMessages_shouldSortEventsByDayAndTime() {
        // Arrange
        CompetitionEntity competition = createTestCompetition();

        var day1 = createDay();
        day1.setDayName("День 1");
        competition.addDay(day1);

        var day2 = createDay();
        day2.setDayName("День 2");
        competition.addDay(day2);

        // Событие в день 2, время 10:00
        EventEntity event1 = createTestEvent();
        event1.setResultUrl(null);
        event1.setEventName("Забіг 1");
        event1.setTime("10:00");
        day2.addEvent(event1);
        event1.setDay(day2);

        HeatEntity heat1 = createTestHeat();
        heat1.setEvent(event1);
        var participant1 = createTestParticipant();
        var heatLine1 = createTestHeatLine(participant1, heat1);
        NotificationQueueEntity notification1 = createTestNotification(TEST_CHAT_ID_1, participant1, heatLine1);
        notification1.setId(1L);

        // Событие в день 1, время 12:00
        EventEntity event2 = createTestEvent();
        event2.setResultUrl(null);
        event2.setEventName("Забіг 2");
        event2.setTime("12:00");
        day1.addEvent(event2);
        event2.setDay(day1);

        HeatEntity heat2 = createTestHeat();
        heat2.setEvent(event2);
        var participant2 = createTestParticipant();
        var heatLine2 = createTestHeatLine(participant2, heat2);
        NotificationQueueEntity notification2 = createTestNotification(TEST_CHAT_ID_1, participant2, heatLine2);
        notification2.setId(2L);

        // Событие в день 1, время 09:00
        EventEntity event3 = createTestEvent();
        event3.setResultUrl(null);
        event3.setEventName("Забіг 3");
        event3.setTime("09:00");
        day1.addEvent(event3);
        event3.setDay(day1);

        HeatEntity heat3 = createTestHeat();
        heat3.setEvent(event3);
        var participant3 = createTestParticipant();
        var heatLine3 = createTestHeatLine(participant3, heat3);
        NotificationQueueEntity notification3 = createTestNotification(TEST_CHAT_ID_1, participant3, heatLine3);
        notification3.setId(3L);

        when(competitionView.nameAndDate(any())).thenReturn("Чемпіонат України, 15.12.23");
        when(competitionView.area(any())).thenReturn("Київ");

        // Act
        var messages = builder.buildAggregatedMessages(
                Map.of(competition, List.of(notification1, notification2, notification3))
        );

        // Assert
        assertThat(messages).hasSize(1);
        var message = messages.get(0);

        String text = message.getText();
        int index3 = text.indexOf("Забіг 3");
        int index2 = text.indexOf("Забіг 2");
        int index1 = text.indexOf("Забіг 1");

        assertThat(index3).isLessThan(index2);
        assertThat(index2).isLessThan(index1);
    }

    @Test
    void buildAggregatedMessages_shouldChunkLargeMessages() {
        // Arrange
        CompetitionEntity competition = createTestCompetition();
        EventEntity event = createTestEvent();
        event.setResultUrl(null);

        var day = createDay();
        day.addEvent(event);
        competition.addDay(day);
        event.setDay(day);

        HeatEntity heat = createTestHeat();
        heat.setEvent(event);

        // Создаем много уведомлений, чтобы превысить лимит
        var notifications = new java.util.ArrayList<NotificationQueueEntity>();
        for (int i = 0; i < 100; i++) {
            var participant = createTestParticipant();
            participant.setName("Ім'я" + i);
            participant.setSurname("Прізвище" + i);
            participant.setUrl("http://example.com/participant/" + i + "/very/long/url/to/increase/message/size");

            var heatLine = createTestHeatLine(participant, heat);
            var notification = createTestNotification(TEST_CHAT_ID_1, participant, heatLine);
            notification.setId((long) i);
            notifications.add(notification);
        }

        when(competitionView.nameAndDate(any())).thenReturn("Чемпіонат України, 15.12.23");
        when(competitionView.area(any())).thenReturn("Київ");

        // Act
        var messages = builder.buildAggregatedMessages(
                Map.of(competition, notifications)
        );

        // Assert
        assertThat(messages).hasSizeGreaterThan(1); // Должно быть разбито на несколько сообщений

        // Все сообщения кроме последнего должны содержать "продовження"
        for (int i = 0; i < messages.size() - 1; i++) {
            assertThat(messages.get(i).getText())
                    .contains("продовження в наступному повідомленні");
        }

        // Последнее сообщение не должно содержать "продовження"
        assertThat(messages.get(messages.size() - 1).getText())
                .doesNotContain("продовження в наступному повідомленні");

        // Все сообщения должны иметь заголовок секции
        for (var message : messages) {
            assertThat(message.getText()).contains("Нові заявки:");
        }

        // Проверяем, что все уведомления учтены
        var allNotificationIds = messages.stream()
                .flatMap(m -> m.getNotificationIds().stream())
                .toList();
        assertThat(allNotificationIds).hasSize(100);
    }

    @Test
    void buildAggregatedMessages_shouldHandleMultipleCompetitions() {
        // Arrange
        CompetitionEntity competition1 = createTestCompetition();
        competition1.setName("Чемпіонат 1");

        CompetitionEntity competition2 = createTestCompetition();
        competition2.setName("Чемпіонат 2");

        // Событие для competition1
        EventEntity event1 = createTestEvent();
        event1.setResultUrl(null);
        var day1 = createDay();
        day1.addEvent(event1);
        competition1.addDay(day1);
        event1.setDay(day1);

        HeatEntity heat1 = createTestHeat();
        heat1.setEvent(event1);
        var participant1 = createTestParticipant();
        var heatLine1 = createTestHeatLine(participant1, heat1);
        NotificationQueueEntity notification1 = createTestNotification(TEST_CHAT_ID_1, participant1, heatLine1);
        notification1.setId(1L);

        // Событие для competition2
        EventEntity event2 = createTestEvent();
        event2.setResultUrl("http://example.com/results");
        var day2 = createDay();
        day2.addEvent(event2);
        competition2.addDay(day2);
        event2.setDay(day2);

        HeatEntity heat2 = createTestHeat();
        heat2.setEvent(event2);
        var participant2 = createTestParticipant();
        var heatLine2 = createTestHeatLine(participant2, heat2);
        NotificationQueueEntity notification2 = createTestNotification(TEST_CHAT_ID_1, participant2, heatLine2);
        notification2.setId(2L);

        when(competitionView.nameAndDate(any())).thenReturn("Змагання, 15.12.23");
        when(competitionView.area(any())).thenReturn("Київ");

        // Act
        var messages = builder.buildAggregatedMessages(
                Map.of(
                        competition1, List.of(notification1),
                        competition2, List.of(notification2)
                )
        );

        // Assert
        assertThat(messages).hasSize(2);

        // Проверяем, что есть сообщение для каждого соревнования
        var competitions = messages.stream()
                .map(NotificationMessage::getCompetition)
                .toList();
        assertThat(competitions).containsExactlyInAnyOrder(competition1, competition2);
    }

}

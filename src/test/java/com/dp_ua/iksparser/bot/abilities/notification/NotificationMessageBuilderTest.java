package com.dp_ua.iksparser.bot.abilities.notification;

import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.TEST_CHAT_ID_1;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.TEST_COMPETITION_URL;
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
    void buildAggregatedMessages_shouldBuildSingleMessageForSingleEvent() {
        // Arrange: создаём реальные сущности через NotificationTestUtils
        CompetitionEntity competition = createTestCompetition();
        EventEntity event = createTestEvent();
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
        assertThat(message
                .getCompetition()).isEqualTo(competition);
        assertThat(message.getNotificationIds()).containsExactly(1L);
        assertThat(message.getText()).contains("Чемпіонат України");
        assertThat(message.getText()).contains(TEST_PARTICIPANT_SURNAME_1 + " " + TEST_PARTICIPANT_NAME_1);
        assertThat(message.getText()).contains("100м вільний стиль, чоловіки");
        assertThat(message.getText()).contains(TEST_COMPETITION_URL);
    }

}

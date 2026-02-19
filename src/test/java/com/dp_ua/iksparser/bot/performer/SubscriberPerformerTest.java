package com.dp_ua.iksparser.bot.performer;

import static com.dp_ua.iksparser.bot.performer.SubscriberPerformerTestUtils.AGGREGATED_DISABLED;
import static com.dp_ua.iksparser.bot.performer.SubscriberPerformerTestUtils.AGGREGATED_ENABLED;
import static com.dp_ua.iksparser.bot.performer.SubscriberPerformerTestUtils.LEGACY_DISABLED;
import static com.dp_ua.iksparser.bot.performer.SubscriberPerformerTestUtils.LEGACY_ENABLED;
import static com.dp_ua.iksparser.bot.performer.SubscriberPerformerTestUtils.SINGLE_INVOCATION;
import static com.dp_ua.iksparser.bot.performer.SubscriberPerformerTestUtils.TEST_HEAT_LINE_ID_1;
import static com.dp_ua.iksparser.bot.performer.SubscriberPerformerTestUtils.TEST_HEAT_LINE_ID_2;
import static com.dp_ua.iksparser.bot.performer.SubscriberPerformerTestUtils.TEST_HEAT_LINE_ID_3;
import static com.dp_ua.iksparser.bot.performer.SubscriberPerformerTestUtils.TEST_PARTICIPANT_ID_1;
import static com.dp_ua.iksparser.bot.performer.SubscriberPerformerTestUtils.TEST_PARTICIPANT_ID_2;
import static com.dp_ua.iksparser.bot.performer.SubscriberPerformerTestUtils.TEST_PARTICIPANT_ID_3;
import static com.dp_ua.iksparser.bot.performer.SubscriberPerformerTestUtils.THREE_INVOCATIONS;
import static com.dp_ua.iksparser.bot.performer.SubscriberPerformerTestUtils.TWO_INVOCATIONS;
import static com.dp_ua.iksparser.bot.performer.SubscriberPerformerTestUtils.TWO_PARTICIPANTS;
import static com.dp_ua.iksparser.bot.performer.SubscriberPerformerTestUtils.createTestHeatLine;
import static com.dp_ua.iksparser.bot.performer.SubscriberPerformerTestUtils.createTestParticipant;
import static com.dp_ua.iksparser.bot.performer.SubscriberPerformerTestUtils.createTestSubscribeEvent;
import static com.dp_ua.iksparser.bot.performer.SubscriberPerformerTestUtils.createTestSubscribeEventWithEmptyParticipations;
import static com.dp_ua.iksparser.bot.performer.SubscriberPerformerTestUtils.createTestSubscribeEventWithMultipleParticipants;
import static com.dp_ua.iksparser.bot.performer.SubscriberPerformerTestUtils.createTestSubscribeEventWithSingleParticipant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dp_ua.iksparser.bot.abilities.notification.NotificationConfigProperties;
import com.dp_ua.iksparser.bot.abilities.notification.NotificationQueueService;
import com.dp_ua.iksparser.bot.abilities.subscribe.SubscribeFacade;
import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;

@ExtendWith(MockitoExtension.class)
class SubscriberPerformerTest {

    @Mock
    private SubscribeFacade facade;

    @Mock
    private NotificationQueueService notificationQueueService;

    @Mock
    private NotificationConfigProperties notificationConfig;

    @InjectMocks
    private SubscriberPerformer subscriberPerformer;

    @Test
    void onApplicationEvent_whenLegacyEnabledOnly_shouldCallFacadeOnly() {
        // Given
        var event = createTestSubscribeEventWithSingleParticipant();

        when(notificationConfig.isLegacyEnabled()).thenReturn(LEGACY_ENABLED);
        when(notificationConfig.isAggregatedEnabled()).thenReturn(AGGREGATED_DISABLED);

        // When
        subscriberPerformer.onApplicationEvent(event);

        // Then
        verify(facade, times(SINGLE_INVOCATION))
                .operateParticipantWithHeatlines(any(ParticipantEntity.class), anyList());
        verify(notificationQueueService, never())
                .saveNotificationsToQueue(any(ParticipantEntity.class), anyList());
    }

    @Test
    void onApplicationEvent_whenAggregatedEnabledOnly_shouldCallQueueServiceOnly() {
        // Given
        var event = createTestSubscribeEventWithSingleParticipant();

        when(notificationConfig.isLegacyEnabled()).thenReturn(LEGACY_DISABLED);
        when(notificationConfig.isAggregatedEnabled()).thenReturn(AGGREGATED_ENABLED);

        // When
        subscriberPerformer.onApplicationEvent(event);

        // Then
        verify(facade, never())
                .operateParticipantWithHeatlines(any(ParticipantEntity.class), anyList());
        verify(notificationQueueService, times(SINGLE_INVOCATION))
                .saveNotificationsToQueue(any(ParticipantEntity.class), anyList());
    }

    @Test
    void onApplicationEvent_whenBothModesEnabled_shouldCallBothServices() {
        // Given
        var event = createTestSubscribeEventWithSingleParticipant();

        when(notificationConfig.isLegacyEnabled()).thenReturn(LEGACY_ENABLED);
        when(notificationConfig.isAggregatedEnabled()).thenReturn(AGGREGATED_ENABLED);

        // When
        subscriberPerformer.onApplicationEvent(event);

        // Then
        verify(facade, times(SINGLE_INVOCATION))
                .operateParticipantWithHeatlines(any(ParticipantEntity.class), anyList());
        verify(notificationQueueService, times(SINGLE_INVOCATION))
                .saveNotificationsToQueue(any(ParticipantEntity.class), anyList());
    }

    @Test
    void onApplicationEvent_whenBothModesDisabled_shouldNotCallAnyService() {
        // Given
        var event = createTestSubscribeEventWithSingleParticipant();

        when(notificationConfig.isLegacyEnabled()).thenReturn(LEGACY_DISABLED);
        when(notificationConfig.isAggregatedEnabled()).thenReturn(AGGREGATED_DISABLED);

        // When
        subscriberPerformer.onApplicationEvent(event);

        // Then
        verify(facade, never())
                .operateParticipantWithHeatlines(any(ParticipantEntity.class), anyList());
        verify(notificationQueueService, never())
                .saveNotificationsToQueue(any(ParticipantEntity.class), anyList());
    }

    @Test
    void onApplicationEvent_whenEmptyParticipations_shouldNotCallAnyService() {
        // Given
        var event = createTestSubscribeEventWithEmptyParticipations();

        // When
        subscriberPerformer.onApplicationEvent(event);

        // Then
        verify(facade, never())
                .operateParticipantWithHeatlines(any(ParticipantEntity.class), anyList());
        verify(notificationQueueService, never())
                .saveNotificationsToQueue(any(ParticipantEntity.class), anyList());
    }

    @Test
    void onApplicationEvent_whenMultipleParticipants_shouldCallServicesForEachParticipant() {
        // Given
        var event = createTestSubscribeEventWithMultipleParticipants();

        when(notificationConfig.isLegacyEnabled()).thenReturn(LEGACY_ENABLED);
        when(notificationConfig.isAggregatedEnabled()).thenReturn(AGGREGATED_ENABLED);

        // When
        subscriberPerformer.onApplicationEvent(event);

        // Then
        verify(facade, times(TWO_INVOCATIONS))
                .operateParticipantWithHeatlines(any(ParticipantEntity.class), anyList());
        verify(notificationQueueService, times(TWO_INVOCATIONS))
                .saveNotificationsToQueue(any(ParticipantEntity.class), anyList());
    }

    @Test
    void onApplicationEvent_shouldPassCorrectParticipantAndHeatLinesToFacade() {
        // Given
        var participant = createTestParticipant(TEST_PARTICIPANT_ID_1);
        var heatLine1 = createTestHeatLine(TEST_HEAT_LINE_ID_1);
        var heatLine2 = createTestHeatLine(TEST_HEAT_LINE_ID_2);
        var heatLines = List.of(heatLine1, heatLine2);

        var event = createTestSubscribeEvent(Map.of(participant, heatLines));

        when(notificationConfig.isLegacyEnabled()).thenReturn(LEGACY_ENABLED);
        when(notificationConfig.isAggregatedEnabled()).thenReturn(AGGREGATED_DISABLED);

        // When
        subscriberPerformer.onApplicationEvent(event);

        // Then
        verify(facade).operateParticipantWithHeatlines(participant, heatLines);
    }

    @Test
    void onApplicationEvent_shouldPassCorrectParticipantAndHeatLinesToQueueService() {
        // Given
        var participant = createTestParticipant(TEST_PARTICIPANT_ID_1);
        var heatLine1 = createTestHeatLine(TEST_HEAT_LINE_ID_1);
        var heatLine2 = createTestHeatLine(TEST_HEAT_LINE_ID_2);
        var heatLines = List.of(heatLine1, heatLine2);

        var event = createTestSubscribeEvent(Map.of(participant, heatLines));

        when(notificationConfig.isLegacyEnabled()).thenReturn(LEGACY_DISABLED);
        when(notificationConfig.isAggregatedEnabled()).thenReturn(AGGREGATED_ENABLED);

        // When
        subscriberPerformer.onApplicationEvent(event);

        // Then
        verify(notificationQueueService).saveNotificationsToQueue(participant, heatLines);
    }

    @Test
    void onApplicationEvent_whenThreeParticipants_shouldCallServicesThreeTimes() {
        // Given
        var participant1 = createTestParticipant(TEST_PARTICIPANT_ID_1);
        var participant2 = createTestParticipant(TEST_PARTICIPANT_ID_2);
        var participant3 = createTestParticipant(TEST_PARTICIPANT_ID_3);

        var heatLines1 = List.of(createTestHeatLine(TEST_HEAT_LINE_ID_1));
        var heatLines2 = List.of(createTestHeatLine(TEST_HEAT_LINE_ID_2));
        var heatLines3 = List.of(createTestHeatLine(TEST_HEAT_LINE_ID_3));

        var event = createTestSubscribeEvent(Map.of(
                participant1, heatLines1,
                participant2, heatLines2,
                participant3, heatLines3
        ));

        when(notificationConfig.isLegacyEnabled()).thenReturn(LEGACY_ENABLED);
        when(notificationConfig.isAggregatedEnabled()).thenReturn(AGGREGATED_ENABLED);

        // When
        subscriberPerformer.onApplicationEvent(event);

        // Then
        verify(facade, times(THREE_INVOCATIONS))
                .operateParticipantWithHeatlines(any(ParticipantEntity.class), anyList());
        verify(notificationQueueService, times(THREE_INVOCATIONS))
                .saveNotificationsToQueue(any(ParticipantEntity.class), anyList());
    }

    @Test
    void onApplicationEvent_shouldProcessAllParticipantsEvenIfOneHasEmptyHeatLines() {
        // Given
        var participant1 = createTestParticipant(TEST_PARTICIPANT_ID_1);
        var participant2 = createTestParticipant(TEST_PARTICIPANT_ID_2);

        var heatLines1 = List.of(createTestHeatLine(TEST_HEAT_LINE_ID_1));
        var emptyHeatLines = List.<HeatLineEntity>of();

        var event = createTestSubscribeEvent(Map.of(
                participant1, heatLines1,
                participant2, emptyHeatLines
        ));

        when(notificationConfig.isLegacyEnabled()).thenReturn(LEGACY_ENABLED);
        when(notificationConfig.isAggregatedEnabled()).thenReturn(AGGREGATED_ENABLED);

        // When
        subscriberPerformer.onApplicationEvent(event);

        // Then
        verify(facade, times(TWO_INVOCATIONS))
                .operateParticipantWithHeatlines(any(ParticipantEntity.class), anyList());
        verify(notificationQueueService, times(TWO_INVOCATIONS))
                .saveNotificationsToQueue(any(ParticipantEntity.class), anyList());

        // Verify specific calls
        verify(facade).operateParticipantWithHeatlines(participant1, heatLines1);
        verify(facade).operateParticipantWithHeatlines(participant2, emptyHeatLines);
    }

    @Test
    void onApplicationEvent_shouldCheckConfigurationFlags() {
        // Given
        var event = createTestSubscribeEventWithSingleParticipant();

        when(notificationConfig.isLegacyEnabled()).thenReturn(LEGACY_ENABLED);
        when(notificationConfig.isAggregatedEnabled()).thenReturn(AGGREGATED_ENABLED);

        // When
        subscriberPerformer.onApplicationEvent(event);

        // Then
        verify(notificationConfig).isLegacyEnabled();
        verify(notificationConfig).isAggregatedEnabled();
    }

    @Test
    void onApplicationEvent_shouldCheckFlagsForEachParticipant() {
        // Given
        var event = createTestSubscribeEventWithMultipleParticipants();

        when(notificationConfig.isLegacyEnabled()).thenReturn(LEGACY_ENABLED);
        when(notificationConfig.isAggregatedEnabled()).thenReturn(AGGREGATED_ENABLED);

        // When
        subscriberPerformer.onApplicationEvent(event);

        // Then
        var participationsSize = event.getParticipations().size();
        assertThat(participationsSize).isEqualTo(TWO_PARTICIPANTS);

        // Each participant should check both flags
        verify(notificationConfig, times(TWO_INVOCATIONS)).isLegacyEnabled();
        verify(notificationConfig, times(TWO_INVOCATIONS)).isAggregatedEnabled();
    }

}

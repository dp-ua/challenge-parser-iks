package com.dp_ua.iksparser.bot.performer;

import java.util.List;
import java.util.Map;

import com.dp_ua.iksparser.bot.event.SubscribeEvent;
import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;

/**
 * Test utilities for SubscriberPerformer unit tests.
 * Contains constants and helper methods for creating test data.
 */
public class SubscriberPerformerTestUtils {

    // Test Constants - Configuration flags
    public static final boolean LEGACY_ENABLED = true;
    public static final boolean LEGACY_DISABLED = false;
    public static final boolean AGGREGATED_ENABLED = true;
    public static final boolean AGGREGATED_DISABLED = false;

    // Test Constants - Participant IDs
    public static final Long TEST_PARTICIPANT_ID_1 = 1L;
    public static final Long TEST_PARTICIPANT_ID_2 = 2L;
    public static final Long TEST_PARTICIPANT_ID_3 = 3L;

    // Test Constants - HeatLine IDs
    public static final Long TEST_HEAT_LINE_ID_1 = 1L;
    public static final Long TEST_HEAT_LINE_ID_2 = 2L;
    public static final Long TEST_HEAT_LINE_ID_3 = 3L;

    // Test Constants - Chat IDs
    public static final String TEST_CHAT_ID = "123456789";

    // Test Constants - Expected invocation counts
    public static final int NO_INVOCATIONS = 0;
    public static final int SINGLE_INVOCATION = 1;
    public static final int TWO_INVOCATIONS = 2;
    public static final int THREE_INVOCATIONS = 3;

    // Test Constants - Collection sizes
    public static final int EMPTY_PARTICIPANTS = 0;
    public static final int SINGLE_PARTICIPANT = 1;
    public static final int TWO_PARTICIPANTS = 2;
    public static final int THREE_PARTICIPANTS = 3;

    public static final int EMPTY_HEAT_LINES = 0;
    public static final int SINGLE_HEAT_LINE = 1;
    public static final int TWO_HEAT_LINES = 2;
    public static final int THREE_HEAT_LINES = 3;
    public static final String PARTICIPANT_NAME_PART = "Participant_";

    /**
     * Creates a test ParticipantEntity with given id
     */
    public static ParticipantEntity createTestParticipant(Long id) {
        var participant = new ParticipantEntity();
        participant.setId(id);
        participant.setName(PARTICIPANT_NAME_PART + id);
        return participant;
    }

    /**
     * Creates a test HeatLineEntity with given id
     */
    public static HeatLineEntity createTestHeatLine(Long id) {
        var heatLine = new HeatLineEntity();
        heatLine.setId(id);
        return heatLine;
    }

    /**
     * Creates a test SubscribeEvent with given participations
     */
    public static SubscribeEvent createTestSubscribeEvent(
            Map<ParticipantEntity, List<HeatLineEntity>> participations
    ) {
        return new SubscribeEvent(TEST_CHAT_ID, participations);
    }

    /**
     * Creates a test SubscribeEvent with single participant and heat lines
     */
    public static SubscribeEvent createTestSubscribeEventWithSingleParticipant() {
        var participant = createTestParticipant(TEST_PARTICIPANT_ID_1);
        var heatLines = List.of(
                createTestHeatLine(TEST_HEAT_LINE_ID_1),
                createTestHeatLine(TEST_HEAT_LINE_ID_2)
        );
        return createTestSubscribeEvent(Map.of(participant, heatLines));
    }

    /**
     * Creates a test SubscribeEvent with multiple participants
     */
    public static SubscribeEvent createTestSubscribeEventWithMultipleParticipants() {
        var participant1 = createTestParticipant(TEST_PARTICIPANT_ID_1);
        var participant2 = createTestParticipant(TEST_PARTICIPANT_ID_2);

        var heatLines1 = List.of(createTestHeatLine(TEST_HEAT_LINE_ID_1));
        var heatLines2 = List.of(createTestHeatLine(TEST_HEAT_LINE_ID_2));

        return createTestSubscribeEvent(Map.of(
                participant1, heatLines1,
                participant2, heatLines2
        ));
    }

    /**
     * Creates a test SubscribeEvent with empty participations
     */
    public static SubscribeEvent createTestSubscribeEventWithEmptyParticipations() {
        return createTestSubscribeEvent(Map.of());
    }

}

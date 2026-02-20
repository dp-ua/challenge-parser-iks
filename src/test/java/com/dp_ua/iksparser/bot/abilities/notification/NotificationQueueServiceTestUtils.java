package com.dp_ua.iksparser.bot.abilities.notification;

import java.util.Arrays;
import java.util.List;

import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import com.dp_ua.iksparser.dba.entity.SubscriberEntity;

/**
 * Test utilities for NotificationQueueService unit tests.
 * Contains constants and helper methods for creating test data and mocks.
 */
public class NotificationQueueServiceTestUtils {

    // Test Constants - Chat IDs
    public static final String TEST_CHAT_ID_1 = "123456";
    public static final String TEST_CHAT_ID_2 = "789012";
    public static final String TEST_CHAT_ID_3 = "555555";

    // Test Constants - Participant IDs
    public static final Long TEST_PARTICIPANT_ID_1 = 1L;

    // Test Constants - HeatLine IDs
    public static final Long TEST_HEAT_LINE_ID_1 = 1L;
    public static final Long TEST_HEAT_LINE_ID_2 = 2L;
    public static final Long TEST_HEAT_LINE_ID_3 = 3L;

    // Test Constants - Collections
    public static final int TWO_SUBSCRIBERS = 2;
    public static final int THREE_SUBSCRIBERS = 3;

    public static final int TWO_HEAT_LINES = 2;
    public static final int THREE_HEAT_LINES = 3;

    // Test Constants - Expected notification counts
    public static final int EXPECTED_NOTIFICATIONS_1_SUB_1_HEAT = 1;
    public static final int EXPECTED_NOTIFICATIONS_1_SUB_2_HEATS = 2;
    public static final int EXPECTED_NOTIFICATIONS_2_SUBS_1_HEAT = 2;
    public static final int EXPECTED_NOTIFICATIONS_2_SUBS_2_HEATS = 4;

    /**
     * Creates a test ParticipantEntity with given id
     */
    public static ParticipantEntity createTestParticipant(Long id) {
        var participant = new ParticipantEntity();
        participant.setId(id);
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
     * Creates a test SubscriberEntity with given chatId
     */
    public static SubscriberEntity createTestSubscriber(String chatId) {
        var subscriber = new SubscriberEntity();
        subscriber.setChatId(chatId);
        return subscriber;
    }

    /**
     * Creates a list of test subscribers
     */
    public static List<SubscriberEntity> createTestSubscribers(String... chatIds) {
        return Arrays.stream(chatIds)
                .map(NotificationQueueServiceTestUtils::createTestSubscriber)
                .toList();
    }

    /**
     * Creates a list of test heat lines
     */
    public static List<HeatLineEntity> createTestHeatLines(Long... ids) {
        return Arrays.stream(ids)
                .map(NotificationQueueServiceTestUtils::createTestHeatLine)
                .toList();
    }

}

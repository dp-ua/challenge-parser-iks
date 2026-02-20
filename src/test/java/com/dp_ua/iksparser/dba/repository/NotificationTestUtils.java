package com.dp_ua.iksparser.dba.repository;

import java.time.LocalDateTime;

import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.entity.CompetitionStatus;
import com.dp_ua.iksparser.dba.entity.DayEntity;
import com.dp_ua.iksparser.dba.entity.EventEntity;
import com.dp_ua.iksparser.dba.entity.HeatEntity;
import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.entity.NotificationQueueEntity;
import com.dp_ua.iksparser.dba.entity.NotificationStatus;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;

/**
 * Test utilities for creating notification-related test data.
 * Contains constants and builder methods to avoid magic strings and numbers.
 */
public class NotificationTestUtils {

    // Test Constants - Chat IDs
    public static final String TEST_CHAT_ID_1 = "123456789";
    public static final String TEST_CHAT_ID_2 = "987654321";
    public static final String TEST_CHAT_ID_3 = "555555555";

    // Test Constants - Participant data
    public static final String TEST_PARTICIPANT_NAME_1 = "Іван";
    public static final String TEST_PARTICIPANT_SURNAME_1 = "Петренко";
    public static final String TEST_PARTICIPANT_NAME_2 = "Марія";
    public static final String TEST_PARTICIPANT_SURNAME_2 = "Коваленко";
    public static final String TEST_PARTICIPANT_TEAM = "Динамо Київ";
    public static final String TEST_PARTICIPANT_REGION = "Київська область";
    public static final String TEST_PARTICIPANT_BORN = "2005";
    public static final String TEST_PARTICIPANT_URL = "https://example.com/participant";

    // Test Constants - Competition data
    public static final String TEST_COMPETITION_NAME_1 = "Чемпіонат України";
    public static final String TEST_COMPETITION_LOCATION = "Київ";
    public static final String TEST_COMPETITION_URL = "https://example.com/competition";

    // Test Constants - HeatLine data
    public static final String TEST_HEAT_LINE_LANE_1 = "4";
    public static final String TEST_HEAT_LINE_LANE_2 = "5";
    public static final String TEST_HEAT_LINE_BIB_1 = "123";
    public static final String TEST_HEAT_LINE_BIB_2 = "456";

    // Test Constants - Heat data
    public static final String TEST_HEAT_DESCRIPTION = "100м вільний стиль, чоловіки";

    // Test Constants - Time
    public static final int DAYS_OLD_FOR_CLEANUP = 7;

    // Test Constants - Notification
    public static final int DEFAULT_RETRY_COUNT = 0;
    public static final int MAX_RETRY_COUNT = 3;
    public static final String TEST_ERROR_MESSAGE = "Test error message";
    public static final long COMPETITION_ID = 1L;

    /**
     * Creates a test ParticipantEntity with default values
     */
    public static ParticipantEntity createTestParticipant() {
        ParticipantEntity participant = new ParticipantEntity();
        participant.setName(TEST_PARTICIPANT_NAME_1);
        participant.setSurname(TEST_PARTICIPANT_SURNAME_1);
        participant.setTeam(TEST_PARTICIPANT_TEAM);
        participant.setRegion(TEST_PARTICIPANT_REGION);
        participant.setBorn(TEST_PARTICIPANT_BORN);
        participant.setUrl(TEST_PARTICIPANT_URL);
        return participant;
    }

    /**
     * Creates a test ParticipantEntity with custom name and surname
     */
    public static ParticipantEntity createTestParticipant(String name, String surname) {
        ParticipantEntity participant = new ParticipantEntity();
        participant.setName(name);
        participant.setSurname(surname);
        participant.setTeam(TEST_PARTICIPANT_TEAM);
        participant.setRegion(TEST_PARTICIPANT_REGION);
        participant.setBorn(TEST_PARTICIPANT_BORN);
        participant.setUrl(TEST_PARTICIPANT_URL);
        return participant;
    }

    /**
     * Creates a test CompetitionEntity with default values
     */
    public static CompetitionEntity createTestCompetition() {
        CompetitionEntity competition = new CompetitionEntity();
        competition.setId(COMPETITION_ID);
        competition.setName(TEST_COMPETITION_NAME_1);
        competition.setCity(TEST_COMPETITION_LOCATION);
        competition.setUrl(TEST_COMPETITION_URL);
        competition.setStatus(CompetitionStatus.C_NOT_STARTED.getName());
        return competition;
    }

    /**
     * Creates a test CompetitionEntity with custom name
     */
    public static CompetitionEntity createTestCompetition(String name) {
        CompetitionEntity competition = new CompetitionEntity();
        competition.setName(name);
        competition.setCity(TEST_COMPETITION_LOCATION);
        competition.setUrl(TEST_COMPETITION_URL);
        competition.setStatus(CompetitionStatus.C_NOT_STARTED.getName());
        return competition;
    }

    /**
     * Creates a test HeatEntity with default values
     */
    public static HeatEntity createTestHeat() {
        HeatEntity heat = new HeatEntity();
        heat.setName(TEST_HEAT_DESCRIPTION);
        return heat;
    }

    public static DayEntity createDay() {
        var day = new DayEntity();
        day.setDayName("День 1");
        return day;
    }

    /**
     * Creates a test EventEntity with default values
     */
    public static EventEntity createTestEvent() {
        return new EventEntity(
                "10:00",
                TEST_HEAT_DESCRIPTION,
                "Men",
                "Final",
                TEST_COMPETITION_URL,
                ""
        );
    }

    /**
     * Creates a test HeatLineEntity with default values
     */
    public static HeatLineEntity createTestHeatLine(ParticipantEntity participant, HeatEntity heat) {
        HeatLineEntity heatLine = new HeatLineEntity();
        heatLine.setParticipant(participant);
        heatLine.setHeat(heat);
        heatLine.setLane(TEST_HEAT_LINE_LANE_1);
        heatLine.setBib(TEST_HEAT_LINE_BIB_1);
        return heatLine;
    }

    /**
     * Creates a test HeatLineEntity with custom lane and bib
     */
    public static HeatLineEntity createTestHeatLine(
            ParticipantEntity participant,
            HeatEntity heat,
            String lane,
            String bib
    ) {
        HeatLineEntity heatLine = new HeatLineEntity();
        heatLine.setParticipant(participant);
        heatLine.setHeat(heat);
        heatLine.setLane(lane);
        heatLine.setBib(bib);
        return heatLine;
    }

    /**
     * Creates a test NotificationQueueEntity with NEW status
     */
    public static NotificationQueueEntity createTestNotification(
            String chatId,
            ParticipantEntity participant,
            HeatLineEntity heatLine
    ) {
        return NotificationQueueEntity.builder()
                .chatId(chatId)
                .participant(participant)
                .heatLine(heatLine)
                .status(NotificationStatus.NEW)
                .retryCount(DEFAULT_RETRY_COUNT)
                .build();
    }

    /**
     * Creates a test NotificationQueueEntity with custom status
     */
    public static NotificationQueueEntity createTestNotification(
            String chatId,
            ParticipantEntity participant,
            HeatLineEntity heatLine,
            NotificationStatus status
    ) {
        return NotificationQueueEntity.builder()
                .chatId(chatId)
                .participant(participant)
                .heatLine(heatLine)
                .status(status)
                .retryCount(DEFAULT_RETRY_COUNT)
                .build();
    }

    /**
     * Creates a test NotificationQueueEntity with SENT status and processedAt
     */
    public static NotificationQueueEntity createSentNotification(
            String chatId,
            ParticipantEntity participant,
            HeatLineEntity heatLine
    ) {
        return NotificationQueueEntity.builder()
                .chatId(chatId)
                .participant(participant)
                .heatLine(heatLine)
                .status(NotificationStatus.SENT)
                .processedAt(LocalDateTime.now())
                .retryCount(DEFAULT_RETRY_COUNT)
                .build();
    }

    /**
     * Creates a test NotificationQueueEntity with ERROR status
     */
    public static NotificationQueueEntity createErrorNotification(
            String chatId,
            ParticipantEntity participant,
            HeatLineEntity heatLine,
            String errorMessage
    ) {
        return NotificationQueueEntity.builder()
                .chatId(chatId)
                .participant(participant)
                .heatLine(heatLine)
                .status(NotificationStatus.ERROR)
                .processedAt(LocalDateTime.now())
                .errorMessage(errorMessage)
                .retryCount(MAX_RETRY_COUNT)
                .build();
    }

}

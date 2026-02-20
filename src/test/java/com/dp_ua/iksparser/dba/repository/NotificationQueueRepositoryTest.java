package com.dp_ua.iksparser.dba.repository;

import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.DAYS_OLD_FOR_CLEANUP;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.DEFAULT_RETRY_COUNT;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.MAX_RETRY_COUNT;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.TEST_CHAT_ID_1;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.TEST_CHAT_ID_2;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.TEST_CHAT_ID_3;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.TEST_COMPETITION_NAME_1;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.TEST_ERROR_MESSAGE;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.TEST_HEAT_LINE_BIB_1;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.TEST_HEAT_LINE_BIB_2;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.TEST_HEAT_LINE_LANE_1;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.TEST_HEAT_LINE_LANE_2;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.TEST_PARTICIPANT_NAME_1;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.TEST_PARTICIPANT_NAME_2;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.TEST_PARTICIPANT_SURNAME_1;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.TEST_PARTICIPANT_SURNAME_2;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.createErrorNotification;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.createSentNotification;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.createTestCompetition;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.createTestEvent;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.createTestHeat;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.createTestHeatLine;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.createTestNotification;
import static com.dp_ua.iksparser.dba.repository.NotificationTestUtils.createTestParticipant;
import static java.time.temporal.ChronoUnit.MICROS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.dp_ua.iksparser.dba.entity.DayEntity;
import com.dp_ua.iksparser.dba.entity.EventEntity;
import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.entity.NotificationQueueEntity;
import com.dp_ua.iksparser.dba.entity.NotificationStatus;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import com.dp_ua.iksparser.dba.repo.CompetitionRepo;
import com.dp_ua.iksparser.dba.repo.EventRepo;
import com.dp_ua.iksparser.dba.repo.HeatLineRepo;
import com.dp_ua.iksparser.dba.repo.HeatRepo;
import com.dp_ua.iksparser.dba.repo.NotificationQueueRepository;
import com.dp_ua.iksparser.dba.repo.ParticipantRepo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@DataJpaTest
class NotificationQueueRepositoryTest {

    @Autowired
    private NotificationQueueRepository notificationQueueRepository;

    @Autowired
    private ParticipantRepo participantRepo;

    @Autowired
    private CompetitionRepo competitionRepo;

    @Autowired
    private HeatRepo heatRepo;

    @Autowired
    private EventRepo eventRepo;

    @Autowired
    private HeatLineRepo heatLineRepo;

    private ParticipantEntity participant1;
    private ParticipantEntity participant2;
    private HeatLineEntity heatLine1;
    private HeatLineEntity heatLine2;

    @BeforeEach
    void setUp() {
        // Clear all data
        notificationQueueRepository.deleteAll();
        heatLineRepo.deleteAll();
        heatRepo.deleteAll();
        eventRepo.deleteAll();
        competitionRepo.deleteAll();
        participantRepo.deleteAll();

        // Create test participants
        participant1 = createTestParticipant(TEST_PARTICIPANT_NAME_1, TEST_PARTICIPANT_SURNAME_1);
        participant1 = participantRepo.save(participant1);

        participant2 = createTestParticipant(TEST_PARTICIPANT_NAME_2, TEST_PARTICIPANT_SURNAME_2);
        participant2 = participantRepo.save(participant2);

        // Create test competitions
        var competition1 = createTestCompetition(TEST_COMPETITION_NAME_1);
        competition1 = competitionRepo.save(competition1);

        // Create test heat
        var heat1 = createTestHeat();
        EventEntity event1 = createTestEvent();
        DayEntity day1 = new DayEntity("2025-03-20", "day1", "День 1", "Day 1");
        day1.setCompetition(competition1);
        day1.addEvent(event1);
        event1.setDay(day1);
        event1.addHeat(heat1);
        heat1.setEvent(event1);
        competition1.addDay(day1);
        competition1 = competitionRepo.save(competition1);
        day1 = competition1.getDays().get(0);
        event1 = day1.getEvents().get(0);
        heat1 = event1.getHeats().get(0);

        // Create test heat lines
        heatLine1 = createTestHeatLine(participant1, heat1, TEST_HEAT_LINE_LANE_1, TEST_HEAT_LINE_BIB_1);
        heatLine1 = heatLineRepo.save(heatLine1);

        heatLine2 = createTestHeatLine(participant2, heat1, TEST_HEAT_LINE_LANE_2, TEST_HEAT_LINE_BIB_2);
        heatLine2 = heatLineRepo.save(heatLine2);
    }

    @Test
    void shouldFindNewNotifications() {
        // Given
        NotificationQueueEntity newNotification = createTestNotification(
                TEST_CHAT_ID_1, participant1, heatLine1
        );
        notificationQueueRepository.save(newNotification);

        NotificationQueueEntity sentNotification = createSentNotification(
                TEST_CHAT_ID_2, participant2, heatLine2
        );
        notificationQueueRepository.save(sentNotification);

        // When
        var found = notificationQueueRepository.findByStatus(NotificationStatus.NEW);

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getChatId()).isEqualTo(TEST_CHAT_ID_1);
        assertThat(found.get(0).getStatus()).isEqualTo(NotificationStatus.NEW);
    }

    @Test
    void shouldFindDistinctChatIdsWithNewNotifications() {
        // Given
        notificationQueueRepository.save(
                createTestNotification(TEST_CHAT_ID_1, participant1, heatLine1)
        );
        notificationQueueRepository.save(
                createTestNotification(TEST_CHAT_ID_1, participant2, heatLine2)
        );
        notificationQueueRepository.save(
                createTestNotification(TEST_CHAT_ID_2, participant1, heatLine1)
        );
        notificationQueueRepository.save(
                createSentNotification(TEST_CHAT_ID_3, participant2, heatLine2)
        );

        // When
        List<String> chatIds = notificationQueueRepository.findDistinctChatIdsByStatus(NotificationStatus.NEW);

        // Then
        assertThat(chatIds)
                .hasSize(2)
                .containsExactlyInAnyOrder(TEST_CHAT_ID_1, TEST_CHAT_ID_2);
    }

    @Test
    void shouldFindByChatIdAndStatus() {
        // Given
        notificationQueueRepository.save(
                createTestNotification(TEST_CHAT_ID_1, participant1, heatLine1)
        );
        notificationQueueRepository.save(
                createTestNotification(TEST_CHAT_ID_1, participant2, heatLine2)
        );
        notificationQueueRepository.save(
                createTestNotification(TEST_CHAT_ID_2, participant1, heatLine1)
        );

        // When
        List<NotificationQueueEntity> found = notificationQueueRepository
                .findByChatIdAndStatus(TEST_CHAT_ID_1, NotificationStatus.NEW);

        // Then
        assertThat(found)
                .hasSize(2)
                .allMatch(n -> n.getChatId().equals(TEST_CHAT_ID_1));
    }

    @Test
    void shouldCheckIfNotificationExists() {
        // Given
        NotificationQueueEntity notification = createTestNotification(
                TEST_CHAT_ID_1, participant1, heatLine1
        );
        notificationQueueRepository.save(notification);

        // When
        boolean exists = notificationQueueRepository.existsByChatIdAndParticipantAndHeatLineAndStatus(
                TEST_CHAT_ID_1,
                participant1,
                heatLine1,
                NotificationStatus.NEW
        );

        boolean notExists = notificationQueueRepository.existsByChatIdAndParticipantAndHeatLineAndStatus(
                TEST_CHAT_ID_2,
                participant1,
                heatLine1,
                NotificationStatus.NEW
        );

        // Then
        assertTrue(exists);
        assertFalse(notExists);
    }

    @Test
    void shouldDeleteOldNotifications() {
        // Given
        NotificationQueueEntity recentNotification = createSentNotification(
                TEST_CHAT_ID_1, participant1, heatLine1
        );
        notificationQueueRepository.save(recentNotification);

        NotificationQueueEntity oldNotification = createSentNotification(
                TEST_CHAT_ID_2, participant2, heatLine2
        );
        notificationQueueRepository.save(oldNotification);

        // When
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(DAYS_OLD_FOR_CLEANUP);
        int deleted = notificationQueueRepository.deleteByProcessedAtBefore(cutoffDate);

        // Then
        // В тестовой среде оба созданы только что, поэтому удалений не будет
        assertThat(deleted).isZero();

        // Проверяем, что все записи остались
        List<NotificationQueueEntity> all = notificationQueueRepository.findAll();
        assertThat(all).hasSize(2);
    }

    @Test
    void shouldCountByStatus() {
        // Given
        notificationQueueRepository.save(
                createTestNotification(TEST_CHAT_ID_1, participant1, heatLine1)
        );
        notificationQueueRepository.save(
                createTestNotification(TEST_CHAT_ID_2, participant2, heatLine2)
        );
        notificationQueueRepository.save(
                createSentNotification(TEST_CHAT_ID_3, participant1, heatLine1)
        );

        // When
        long newCount = notificationQueueRepository.countByStatus(NotificationStatus.NEW);
        long sentCount = notificationQueueRepository.countByStatus(NotificationStatus.SENT);

        // Then
        assertThat(newCount).isEqualTo(2);
        assertThat(sentCount).isEqualTo(1);
    }

    @Test
    void shouldHandleErrorNotifications() {
        // Given
        NotificationQueueEntity errorNotification = createErrorNotification(
                TEST_CHAT_ID_1, participant1, heatLine1, TEST_ERROR_MESSAGE
        );
        notificationQueueRepository.save(errorNotification);

        // When
        List<NotificationQueueEntity> errors = notificationQueueRepository
                .findByStatus(NotificationStatus.ERROR);

        // Then
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getErrorMessage()).isEqualTo(TEST_ERROR_MESSAGE);
        assertThat(errors.get(0).getRetryCount()).isEqualTo(MAX_RETRY_COUNT);
    }

    @Test
    void shouldAutomaticallySetDefaultValues() {
        // Given
        NotificationQueueEntity notification = NotificationQueueEntity.builder()
                .chatId(TEST_CHAT_ID_1)
                .participant(participant1)
                .heatLine(heatLine1)
                .build();

        // When
        NotificationQueueEntity saved = notificationQueueRepository.save(notification);

        // Then
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.NEW);
        assertThat(saved.getRetryCount()).isEqualTo(DEFAULT_RETRY_COUNT);
        assertThat(saved.getCreated()).isNotNull();
    }

    @Test
    void shouldUpdateStatusForChatAtomically() {
        // Given
        notificationQueueRepository.save(
                createTestNotification(TEST_CHAT_ID_1, participant1, heatLine1)
        );
        notificationQueueRepository.save(
                createTestNotification(TEST_CHAT_ID_1, participant2, heatLine2)
        );
        notificationQueueRepository.save(
                createTestNotification(TEST_CHAT_ID_2, participant1, heatLine1)
        );

        // When
        LocalDateTime updateTime = LocalDateTime.now();
        int updated = notificationQueueRepository.updateStatusForChat(
                TEST_CHAT_ID_1,
                NotificationStatus.NEW,
                NotificationStatus.PROCESSING,
                updateTime
        );
        log.info("Update time: {}", updateTime);
        // Then
        assertThat(updated).isEqualTo(2);

        // Verify status changed for chat1
        List<NotificationQueueEntity> chat1Notifications =
                notificationQueueRepository.findByChatIdAndStatus(TEST_CHAT_ID_1, NotificationStatus.PROCESSING);
        assertThat(chat1Notifications)
                .hasSize(2)
                .allMatch(n -> n.getStatus() == NotificationStatus.PROCESSING)
                .allMatch(n -> n.getUpdated() != null
                        && n.getUpdated().truncatedTo(MICROS).isEqual(updateTime.truncatedTo(MICROS))
                )
        ;

        // Verify chat2 still has NEW status
        List<NotificationQueueEntity> chat2Notifications =
                notificationQueueRepository.findByChatIdAndStatus(TEST_CHAT_ID_2, NotificationStatus.NEW);
        assertThat(chat2Notifications).hasSize(1);
    }

    @Test
    void shouldNotUpdateIfStatusDoesNotMatch() {
        // Given
        notificationQueueRepository.save(
                createTestNotification(TEST_CHAT_ID_1, participant1, heatLine1)
        );

        // When - trying to update from PROCESSING to SENT, but current status is NEW
        int updated = notificationQueueRepository.updateStatusForChat(
                TEST_CHAT_ID_1,
                NotificationStatus.PROCESSING,
                NotificationStatus.SENT,
                LocalDateTime.now()
        );

        // Then
        assertThat(updated).isZero();

        // Verify status is still NEW
        List<NotificationQueueEntity> notifications =
                notificationQueueRepository.findByChatIdAndStatus(TEST_CHAT_ID_1, NotificationStatus.NEW);
        assertThat(notifications).hasSize(1);
    }

    @Test
    void shouldUpdateStatusBatch() {
        // Given
        NotificationQueueEntity notif1 = notificationQueueRepository.save(
                createTestNotification(TEST_CHAT_ID_1, participant1, heatLine1)
        );
        NotificationQueueEntity notif2 = notificationQueueRepository.save(
                createTestNotification(TEST_CHAT_ID_1, participant2, heatLine2)
        );
        NotificationQueueEntity notif3 = notificationQueueRepository.save(
                createTestNotification(TEST_CHAT_ID_2, participant1, heatLine1)
        );

        List<Long> idsToUpdate = List.of(notif1.getId(), notif2.getId());

        // When
        LocalDateTime processedTime = LocalDateTime.now();
        int updated = notificationQueueRepository.updateStatusBatch(
                idsToUpdate,
                NotificationStatus.SENT,
                processedTime,
                processedTime
        );

        // Then
        assertThat(updated).isEqualTo(2);

        // Verify updated notifications
        List<NotificationQueueEntity> sentNotifications =
                notificationQueueRepository.findByStatus(NotificationStatus.SENT);
        assertThat(sentNotifications)
                .hasSize(2)
                .extracting(NotificationQueueEntity::getId)
                .containsExactlyInAnyOrder(notif1.getId(), notif2.getId());

        assertThat(sentNotifications)
                .allMatch(n -> n.getProcessedAt() != null)
                .allMatch(n -> n.getUpdated() != null);

        // Verify notif3 is still NEW
        NotificationQueueEntity notif3After = notificationQueueRepository.findById(notif3.getId()).orElseThrow();
        assertThat(notif3After.getStatus()).isEqualTo(NotificationStatus.NEW);
    }

    @Test
    void shouldUpdateStatusWithError() {
        // Given
        NotificationQueueEntity notif1 = notificationQueueRepository.save(
                createTestNotification(TEST_CHAT_ID_1, participant1, heatLine1)
        );
        NotificationQueueEntity notif2 = notificationQueueRepository.save(
                createTestNotification(TEST_CHAT_ID_1, participant2, heatLine2)
        );

        List<Long> idsToUpdate = List.of(notif1.getId(), notif2.getId());
        String errorMsg = "Connection timeout";

        // When
        LocalDateTime errorTime = LocalDateTime.now();
        int updated = notificationQueueRepository.updateStatusWithError(
                idsToUpdate,
                NotificationStatus.ERROR,
                errorTime,
                errorMsg,
                errorTime
        );

        // Then
        assertThat(updated).isEqualTo(2);

        // Verify error notifications
        List<NotificationQueueEntity> errorNotifications =
                notificationQueueRepository.findByStatus(NotificationStatus.ERROR);
        assertThat(errorNotifications).hasSize(2);

        errorNotifications.forEach(notification -> {
            assertThat(notification.getErrorMessage()).isEqualTo(errorMsg);
            assertThat(notification.getRetryCount()).isEqualTo(1); // incremented from 0
            assertThat(notification.getProcessedAt()).isNotNull();
            assertThat(notification.getUpdated()).isNotNull();
        });
    }

    @Test
    void shouldIncrementRetryCountOnMultipleErrors() {
        // Given
        NotificationQueueEntity notif = notificationQueueRepository.save(
                createTestNotification(TEST_CHAT_ID_1, participant1, heatLine1)
        );

        // When - first error
        notificationQueueRepository.updateStatusWithError(
                List.of(notif.getId()),
                NotificationStatus.ERROR,
                LocalDateTime.now(),
                "Error 1",
                LocalDateTime.now()
        );

        NotificationQueueEntity afterFirstError = notificationQueueRepository.findById(notif.getId()).orElseThrow();
        assertThat(afterFirstError.getRetryCount()).isEqualTo(1);

        // When - second error (retry)
        notificationQueueRepository.updateStatusWithError(
                List.of(notif.getId()),
                NotificationStatus.ERROR,
                LocalDateTime.now(),
                "Error 2",
                LocalDateTime.now()
        );

        // Then
        NotificationQueueEntity afterSecondError = notificationQueueRepository.findById(notif.getId()).orElseThrow();
        assertThat(afterSecondError.getRetryCount()).isEqualTo(2);
        assertThat(afterSecondError.getErrorMessage()).isEqualTo("Error 2");
    }

    @Test
    void shouldFindByChatIdAndStatusWithDetails() {
        // Given
        NotificationQueueEntity notif1 = createTestNotification(TEST_CHAT_ID_1, participant1, heatLine1);
        notif1.setStatus(NotificationStatus.PROCESSING);
        notificationQueueRepository.save(notif1);

        NotificationQueueEntity notif2 = createTestNotification(TEST_CHAT_ID_1, participant2, heatLine2);
        notif2.setStatus(NotificationStatus.PROCESSING);
        notificationQueueRepository.save(notif2);

        NotificationQueueEntity notif3 = createTestNotification(TEST_CHAT_ID_2, participant1, heatLine1);
        notif3.setStatus(NotificationStatus.PROCESSING);
        notificationQueueRepository.save(notif3);

        // When
        List<NotificationQueueEntity> found = notificationQueueRepository
                .findByChatIdAndStatusWithDetails(TEST_CHAT_ID_1, NotificationStatus.PROCESSING);

        // Then
        assertThat(found).hasSize(2);

        // Verify eager loading worked (no lazy initialization exception)
        found.forEach(notification -> {
            assertThat(notification.getParticipant()).isNotNull();
            assertThat(notification.getParticipant().getName()).isNotNull();
            assertThat(notification.getHeatLine()).isNotNull();
            assertThat(notification.getHeatLine().getBib()).isNotNull();
        });
    }

    @Test
    void shouldResetStatusBatch() {
        // Given
        NotificationQueueEntity notif1 = createTestNotification(TEST_CHAT_ID_1, participant1, heatLine1);
        notif1.setStatus(NotificationStatus.PROCESSING);
        notif1 = notificationQueueRepository.save(notif1);

        NotificationQueueEntity notif2 = createTestNotification(TEST_CHAT_ID_1, participant2, heatLine2);
        notif2.setStatus(NotificationStatus.PROCESSING);
        notif2 = notificationQueueRepository.save(notif2);

        List<Long> idsToReset = List.of(notif1.getId(), notif2.getId());

        // When
        LocalDateTime resetTime = LocalDateTime.now();
        int updated = notificationQueueRepository.resetStatusBatch(
                idsToReset,
                NotificationStatus.NEW,
                resetTime
        );

        // Then
        assertThat(updated).isEqualTo(2);

        // Verify status reset to NEW
        List<NotificationQueueEntity> resetNotifications =
                notificationQueueRepository.findByStatus(NotificationStatus.NEW);
        assertThat(resetNotifications)
                .hasSize(2)
                .extracting(NotificationQueueEntity::getId)
                .containsExactlyInAnyOrder(notif1.getId(), notif2.getId());
    }

    @Test
    void shouldHandleEmptyListInBatchOperations() {
        // Given
        List<Long> emptyList = List.of();

        // When
        int updatedBatch = notificationQueueRepository.updateStatusBatch(
                emptyList,
                NotificationStatus.SENT,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        int updatedError = notificationQueueRepository.updateStatusWithError(
                emptyList,
                NotificationStatus.ERROR,
                LocalDateTime.now(),
                "error",
                LocalDateTime.now()
        );

        int updatedReset = notificationQueueRepository.resetStatusBatch(
                emptyList,
                NotificationStatus.NEW,
                LocalDateTime.now()
        );

        // Then
        assertThat(updatedBatch).isZero();
        assertThat(updatedError).isZero();
        assertThat(updatedReset).isZero();
    }

    @Test
    void shouldDeleteOldProcessedNotifications_RealScenario() {
        // Given - create old notification manually setting processedAt
        NotificationQueueEntity oldNotif = NotificationQueueEntity.builder()
                .chatId(TEST_CHAT_ID_1)
                .participant(participant1)
                .heatLine(heatLine1)
                .status(NotificationStatus.SENT)
                .processedAt(LocalDateTime.now().minusDays(10))
                .build();
        notificationQueueRepository.save(oldNotif);

        NotificationQueueEntity recentNotif = createSentNotification(
                TEST_CHAT_ID_2, participant2, heatLine2
        );
        notificationQueueRepository.save(recentNotif);

        // When
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
        int deleted = notificationQueueRepository.deleteByProcessedAtBefore(cutoffDate);

        // Then
        assertThat(deleted).isEqualTo(1);

        List<NotificationQueueEntity> remaining = notificationQueueRepository.findAll();
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).getChatId()).isEqualTo(TEST_CHAT_ID_2);
    }

    @Test
    void shouldHandleConcurrentUpdateScenario() {
        // Given - simulate scenario where two processes try to update same chat
        NotificationQueueEntity notif = notificationQueueRepository.save(
                createTestNotification(TEST_CHAT_ID_1, participant1, heatLine1)
        );

        // When - first process updates successfully
        int firstUpdate = notificationQueueRepository.updateStatusForChat(
                TEST_CHAT_ID_1,
                NotificationStatus.NEW,
                NotificationStatus.PROCESSING,
                LocalDateTime.now()
        );

        // Second process tries to update same notification (should fail - already PROCESSING)
        int secondUpdate = notificationQueueRepository.updateStatusForChat(
                TEST_CHAT_ID_1,
                NotificationStatus.NEW,
                NotificationStatus.PROCESSING,
                LocalDateTime.now()
        );

        // Then
        assertThat(firstUpdate).isEqualTo(1);
        assertThat(secondUpdate).isZero(); // No update because status is already PROCESSING

        // Verify final state
        NotificationQueueEntity finalState = notificationQueueRepository.findById(notif.getId()).orElseThrow();
        assertThat(finalState.getStatus()).isEqualTo(NotificationStatus.PROCESSING);
    }

}

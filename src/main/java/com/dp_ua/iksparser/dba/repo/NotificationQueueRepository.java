package com.dp_ua.iksparser.dba.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.entity.NotificationQueueEntity;
import com.dp_ua.iksparser.dba.entity.NotificationStatus;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;

/**
 * Repository for NotificationQueueEntity.
 * Provides methods for managing notification queue.
 */
@Repository
public interface NotificationQueueRepository extends JpaRepository<NotificationQueueEntity, Long> {

    /**
     * Find all notifications with specific status
     */
    List<NotificationQueueEntity> findByStatus(NotificationStatus status);

    /**
     * Find all notifications for specific chat ID and status
     */
    List<NotificationQueueEntity> findByChatIdAndStatus(String chatId, NotificationStatus status);

    /**
     * Find distinct chat IDs that have notifications with specific status
     */
    @Query("SELECT DISTINCT n.chatId FROM NotificationQueueEntity n WHERE n.status = :status")
    List<String> findDistinctChatIdsByStatus(@Param("status") NotificationStatus status);

    /**
     * Check if notification already exists for specific chat, participant, heatLine and status
     * Used for avoiding duplicates
     */
    boolean existsByChatIdAndParticipantAndHeatLineAndStatus(
            String chatId,
            ParticipantEntity participant,
            HeatLineEntity heatLine,
            NotificationStatus status
    );

    /**
     * Delete old processed notifications (cleanup)
     * Returns count of deleted records
     */
    @Modifying
    @Query("DELETE FROM NotificationQueueEntity n WHERE n.processedAt < :date")
    int deleteByProcessedAtBefore(@Param("date") LocalDateTime date);

    /**
     * Count notifications by status
     */
    long countByStatus(NotificationStatus status);

    /**
     * Find notifications by chat ID, ordered by competition and heat
     */
    @Query("SELECT n FROM NotificationQueueEntity n " +
            "WHERE n.chatId = :chatId AND n.status = :status " +
            "ORDER BY n.competition.id, n.heatLine.heat.id")
    List<NotificationQueueEntity> findByChatIdAndStatusOrderByCompetitionAndHeat(
            @Param("chatId") String chatId,
            @Param("status") NotificationStatus status
    );
}

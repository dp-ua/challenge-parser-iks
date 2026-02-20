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
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM NotificationQueueEntity n WHERE n.processedAt < :date")
    int deleteByProcessedAtBefore(@Param("date") LocalDateTime date);

    /**
     * Count notifications by status
     */
    long countByStatus(NotificationStatus status);

    /**
     * Атомарно изменяет статус с NEW на PROCESSING для конкретного chatId
     * Явно передаем updated время для совместимости с разными БД
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE NotificationQueueEntity n " +
            "SET n.status = :newStatus, n.updated = :updatedAt " +
            "WHERE n.chatId = :chatId AND n.status = :oldStatus")
    int updateStatusForChat(
            @Param("chatId") String chatId,
            @Param("oldStatus") NotificationStatus oldStatus,
            @Param("newStatus") NotificationStatus newStatus,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * Атомарно изменяет статус для списка ID
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE NotificationQueueEntity n " +
            "SET n.status = :status, n.processedAt = :processedAt, n.updated = :updatedAt " +
            "WHERE n.id IN :ids")
    int updateStatusBatch(
            @Param("ids") List<Long> ids,
            @Param("status") NotificationStatus status,
            @Param("processedAt") LocalDateTime processedAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * Обновление статуса с увеличением retry счетчика
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE NotificationQueueEntity n " +
            "SET n.status = :status, " +
            "    n.processedAt = :processedAt, " +
            "    n.errorMessage = :errorMessage, " +
            "    n.retryCount = n.retryCount + 1, " +
            "    n.updated = :updatedAt " +
            "WHERE n.id IN :ids")
    int updateStatusWithError(
            @Param("ids") List<Long> ids,
            @Param("status") NotificationStatus status,
            @Param("processedAt") LocalDateTime processedAt,
            @Param("errorMessage") String errorMessage,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * Найти все уведомления для chatId со статусом PROCESSING
     * Eager fetch для избежания N+1 проблемы
     */
    @Query("""
                SELECT n FROM NotificationQueueEntity n
                JOIN FETCH n.participant p
                JOIN FETCH n.heatLine hl
                JOIN FETCH hl.heat h
                JOIN FETCH h.event e
                JOIN FETCH e.day d
                JOIN FETCH d.competition c
                WHERE n.chatId = :chatId AND n.status = :status
            """)
    List<NotificationQueueEntity> findByChatIdAndStatusWithDetails(
            @Param("chatId") String chatId,
            @Param("status") NotificationStatus status
    );

    /**
     * Сброс статуса для "застрявших" PROCESSING уведомлений
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE NotificationQueueEntity n " +
            "SET n.status = :newStatus, n.updated = :updatedAt " +
            "WHERE n.id IN :ids")
    int resetStatusBatch(
            @Param("ids") List<Long> ids,
            @Param("newStatus") NotificationStatus newStatus,
            @Param("updatedAt") LocalDateTime updatedAt
    );

}

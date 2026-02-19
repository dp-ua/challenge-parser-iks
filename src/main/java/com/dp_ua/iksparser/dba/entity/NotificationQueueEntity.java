package com.dp_ua.iksparser.dba.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity for storing notification queue.
 * Used for aggregated notifications to subscribers.
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "notification_queue", indexes = {
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_chat_id_status", columnList = "chatId, status"),
        @Index(name = "idx_created_at", columnList = "created")
})
public class NotificationQueueEntity extends DomainElement {

    /**
     * Telegram chat ID of the subscriber
     */
    @Column(nullable = false)
    private String chatId;

    /**
     * Participant who has new heat lines
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private ParticipantEntity participant;

    /**
     * Heat line with new information
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "heat_line_id", nullable = false)
    private HeatLineEntity heatLine;

    /**
     * Competition for grouping
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_id", nullable = false)
    private CompetitionEntity competition;

    /**
     * Status of notification processing
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    /**
     * When the notification was processed (sent or error)
     */
    private LocalDateTime processedAt;

    /**
     * Error message if processing failed
     */
    @Column(length = 1000)
    private String errorMessage;

    /**
     * Retry count for failed notifications
     */
    @Builder.Default
    private Integer retryCount = 0;

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (status == null) {
            status = NotificationStatus.NEW;
        }
        if (retryCount == null) {
            retryCount = 0;
        }
    }

    @Override
    public String toString() {
        return "NotificationQueueEntity{" +
                "id=" + id +
                ", chatId='" + chatId + '\'' +
                ", status=" + status +
                ", created=" + created +
                ", processedAt=" + processedAt +
                '}';
    }

}

package com.dp_ua.iksparser.bot.abilities.notification;

import java.util.List;

import com.dp_ua.iksparser.dba.entity.CompetitionEntity;

import lombok.Builder;
import lombok.Data;

/**
 * Single notification message for one competition
 */
@Data
@Builder
public class NotificationMessage {

    /**
     * Formatted message text
     */
    private String text;

    /**
     * Competition this message is about
     */
    private CompetitionEntity competition;

    /**
     * List of notification queue IDs included in this message
     */
    private List<Long> notificationIds;

}

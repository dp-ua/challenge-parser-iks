package com.dp_ua.iksparser.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Configuration properties for competitions handling.
 */
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "competition")
public class CompetitionProperties {

    /**
     * A competition is considered stale when it ended more than this number of months ago
     * AND has at least {@link #staleFailedDays} days with failed updates.
     * Stale competitions are excluded from automatic updates (manual update by admin is still possible).
     */
    @Positive(message = "competition.stale-after-months must be positive")
    private int staleAfterMonths = 3;

    /**
     * Number of days with failed updates (since the last success) required to treat an old competition as stale.
     */
    @Positive(message = "competition.stale-failed-days must be positive")
    private int staleFailedDays = 20;

    /**
     * Only competitions that ended no more than this number of days ago (or are not finished yet)
     * are polled by the jobs that run every few minutes.
     */
    @Positive(message = "competition.frequent-update-window-days must be positive")
    private int frequentUpdateWindowDays = 7;

}

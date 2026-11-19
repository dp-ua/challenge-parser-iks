package com.dp_ua.iksparser.service;

import static com.dp_ua.iksparser.configuration.Constants.FORMATTER;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.dp_ua.iksparser.configuration.CompetitionProperties;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Decides which competitions are worth updating automatically.
 * <ul>
 *   <li><b>Frequent window</b>: ended no more than {@code competition.frequent-update-window-days} days ago
 *   (or not ended yet). Only these are polled by the jobs that run every few minutes.</li>
 *   <li><b>Frozen</b>: ended more than {@code competition.stale-after-months} months ago and already loaded.
 *   Nothing changes there anymore, so the daily job skips them (manual update still works).</li>
 *   <li><b>Stale</b>: ended more than {@code competition.stale-after-months} months ago and failed to update
 *   on at least {@code competition.stale-failed-days} different days.</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompetitionUpdatePolicy {

    private final CompetitionProperties properties;

    public boolean isStale(CompetitionEntity competition) {
        return isStale(competition, LocalDate.now());
    }

    public boolean isStale(CompetitionEntity competition, LocalDate today) {
        return competition.getFailedUpdateDays() >= properties.getStaleFailedDays()
                && isOld(competition, today);
    }

    /**
     * @return true if the competition is still interesting for frequent polling.
     * Competitions with unknown end date are not in the window (the daily job still handles them).
     */
    public boolean isInFrequentWindow(CompetitionEntity competition, LocalDate today) {
        return parseEndDate(competition)
                .map(end -> !end.isBefore(today.minusDays(properties.getFrequentUpdateWindowDays())))
                .orElse(false);
    }

    /**
     * @param filled true if the competition already has loaded days
     */
    public boolean isFrozen(CompetitionEntity competition, LocalDate today, boolean filled) {
        return filled && isOld(competition, today);
    }

    private boolean isOld(CompetitionEntity competition, LocalDate today) {
        return parseEndDate(competition)
                .map(end -> end.isBefore(today.minusMonths(properties.getStaleAfterMonths())))
                .orElse(false);
    }

    private Optional<LocalDate> parseEndDate(CompetitionEntity competition) {
        String endDate = competition.getEndDate();
        if (StringUtils.isBlank(endDate)) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(endDate, FORMATTER));
        } catch (DateTimeParseException e) {
            log.warn("[{}] Can't parse end date '{}'", competition.getId(), endDate);
            return Optional.empty();
        }
    }

}

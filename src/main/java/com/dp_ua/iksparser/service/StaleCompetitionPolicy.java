package com.dp_ua.iksparser.service;

import static com.dp_ua.iksparser.configuration.Constants.FORMATTER;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.dp_ua.iksparser.configuration.CompetitionProperties;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Decides whether a competition is too old for automatic updates.
 * Stale = ended more than {@code competition.stale-after-months} months ago
 * and failed to update on at least {@code competition.stale-failed-days} different days.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StaleCompetitionPolicy {

    private final CompetitionProperties properties;

    public boolean isStale(CompetitionEntity competition) {
        return isStale(competition, LocalDate.now());
    }

    public boolean isStale(CompetitionEntity competition, LocalDate today) {
        if (competition.getFailedUpdateDays() < properties.getStaleFailedDays()) {
            return false;
        }
        String endDate = competition.getEndDate();
        if (StringUtils.isBlank(endDate)) {
            return false;
        }
        try {
            LocalDate end = LocalDate.parse(endDate, FORMATTER);
            return end.isBefore(today.minusMonths(properties.getStaleAfterMonths()));
        } catch (DateTimeParseException e) {
            log.warn("[{}] Can't parse end date '{}', competition is not treated as stale",
                    competition.getId(), endDate);
            return false;
        }
    }

}

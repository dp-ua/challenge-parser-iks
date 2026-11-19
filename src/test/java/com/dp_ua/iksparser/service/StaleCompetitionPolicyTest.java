package com.dp_ua.iksparser.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dp_ua.iksparser.configuration.CompetitionProperties;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;

class StaleCompetitionPolicyTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 18);

    private StaleCompetitionPolicy policy;

    @BeforeEach
    void setUp() {
        CompetitionProperties properties = new CompetitionProperties();
        properties.setStaleAfterMonths(3);
        policy = new StaleCompetitionPolicy(properties);
    }

    private CompetitionEntity withEndDate(String endDate) {
        CompetitionEntity competition = new CompetitionEntity();
        competition.setEndDate(endDate);
        competition.setFailedUpdateDays(20);
        return competition;
    }

    @Test
    void endedMoreThanThreeMonthsAgo_isStale() {
        assertTrue(policy.isStale(withEndDate("17.08.2026"), TODAY));
    }

    @Test
    void oldButFewerThanTwentyFailedDays_isNotStale() {
        CompetitionEntity competition = withEndDate("17.08.2026");
        competition.setFailedUpdateDays(19);
        assertFalse(policy.isStale(competition, TODAY));
    }

    @Test
    void oldAndNeverFailed_isNotStale() {
        CompetitionEntity competition = withEndDate("17.08.2026");
        competition.setFailedUpdateDays(null);
        assertFalse(policy.isStale(competition, TODAY));
    }

    @Test
    void endedExactlyThreeMonthsAgo_isNotStale() {
        assertFalse(policy.isStale(withEndDate("18.08.2026"), TODAY));
    }

    @Test
    void endedRecently_isNotStale() {
        assertFalse(policy.isStale(withEndDate("10.11.2026"), TODAY));
    }

    @Test
    void endsInFuture_isNotStale() {
        assertFalse(policy.isStale(withEndDate("20.12.2026"), TODAY));
    }

    @Test
    void emptyOrNullEndDate_isNotStale() {
        assertFalse(policy.isStale(withEndDate(""), TODAY));
        assertFalse(policy.isStale(withEndDate(null), TODAY));
    }

    @Test
    void unparsableEndDate_isNotStale() {
        assertFalse(policy.isStale(withEndDate("not a date"), TODAY));
    }
}

package com.dp_ua.iksparser.dba.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class CompetitionEntityUpdateFailuresTest {

    private static final LocalDate DAY1 = LocalDate.of(2026, 11, 18);

    @Test
    void newCompetition_hasZeroFailedDays() {
        assertEquals(0, new CompetitionEntity().getFailedUpdateDays());
    }

    @Test
    void repeatedFailuresSameDay_countOnce() {
        CompetitionEntity c = new CompetitionEntity();
        c.registerUpdateFailure(DAY1);
        c.registerUpdateFailure(DAY1);
        c.registerUpdateFailure(DAY1);
        assertEquals(1, c.getFailedUpdateDays());
    }

    @Test
    void failuresOnDifferentDays_accumulate() {
        CompetitionEntity c = new CompetitionEntity();
        c.registerUpdateFailure(DAY1);
        c.registerUpdateFailure(DAY1.plusDays(1));
        c.registerUpdateFailure(DAY1.plusDays(2));
        assertEquals(3, c.getFailedUpdateDays());
    }

    @Test
    void reset_clearsCounterAndDate() {
        CompetitionEntity c = new CompetitionEntity();
        c.registerUpdateFailure(DAY1);
        c.resetUpdateFailures();
        assertEquals(0, c.getFailedUpdateDays());
        assertNull(c.getLastFailedUpdateDate());
        c.registerUpdateFailure(DAY1);
        assertEquals(1, c.getFailedUpdateDays());
    }
}

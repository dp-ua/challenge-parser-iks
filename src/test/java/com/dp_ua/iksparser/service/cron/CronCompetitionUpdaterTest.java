package com.dp_ua.iksparser.service.cron;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dp_ua.iksparser.bot.abilities.competition.CompetitionFacade;
import com.dp_ua.iksparser.configuration.CompetitionProperties;
import com.dp_ua.iksparser.configuration.Constants;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.service.CompetitionService;
import com.dp_ua.iksparser.service.CompetitionUpdatePolicy;
import com.dp_ua.iksparser.service.CompetitionUpdateQueue;

class CronCompetitionUpdaterTest {

    private CompetitionService competitionService;
    private CompetitionUpdateQueue updateQueue;
    private CronCompetitionUpdater updater;

    @BeforeEach
    void setUp() {
        competitionService = mock(CompetitionService.class);
        when(competitionService.getParsedDate(anyString())).thenCallRealMethod();
        updateQueue = mock(CompetitionUpdateQueue.class);
        CompetitionUpdatePolicy policy = new CompetitionUpdatePolicy(new CompetitionProperties());
        updater = new CronCompetitionUpdater(competitionService,
                mock(CompetitionFacade.class), updateQueue, policy);
    }

    private static String date(LocalDate date) {
        return date.format(Constants.FORMATTER);
    }

    private CompetitionEntity competition(long id, LocalDate begin, LocalDate end, String status) {
        CompetitionEntity c = new CompetitionEntity();
        c.setId(id);
        c.setUrl("https://example.com/c" + id);
        c.setBeginDate(date(begin));
        c.setEndDate(date(end));
        c.setStatus(status);
        return c;
    }

    private CompetitionEntity finishedCompetition(long id, LocalDate end) {
        return competition(id, end.minusDays(1), end, "Завершено");
    }

    // ---- daily job ----

    @Test
    void dailyUpdate_skipsStaleCompetitions() {
        CompetitionEntity stale = finishedCompetition(1, LocalDate.now().minusMonths(4));
        stale.setFailedUpdateDays(20);
        CompetitionEntity fresh = finishedCompetition(2, LocalDate.now().minusDays(5));
        when(competitionService.findAllOrderByUpdated()).thenReturn(List.of(stale, fresh));
        when(competitionService.findFilledCompetitionIds()).thenReturn(Set.of());

        updater.updateAllCompetitionsDetails();

        verify(updateQueue, times(1)).requestUpdate(anyLong());
        verify(updateQueue).requestUpdate(2L);
    }

    @Test
    void dailyUpdate_skipsFrozenCompetitions_butKeepsOldNotLoadedAndRecent() {
        CompetitionEntity oldFilled = finishedCompetition(1, LocalDate.now().minusMonths(6));
        CompetitionEntity oldNotLoaded = finishedCompetition(2, LocalDate.now().minusMonths(6));
        CompetitionEntity recentFilled = finishedCompetition(3, LocalDate.now().minusDays(30));
        when(competitionService.findAllOrderByUpdated()).thenReturn(List.of(oldFilled, oldNotLoaded, recentFilled));
        when(competitionService.findFilledCompetitionIds()).thenReturn(Set.of(1L, 3L));

        updater.updateAllCompetitionsDetails();

        verify(updateQueue, never()).requestUpdate(1L);
        verify(updateQueue).requestUpdate(2L);
        verify(updateQueue).requestUpdate(3L);
    }

    // ---- every 9 minutes: competitions with events without links ----

    @Test
    void notFilledEvents_onlyCompetitionsFromFrequentWindow() {
        CompetitionEntity old = finishedCompetition(1, LocalDate.now().minusYears(1));
        CompetitionEntity recent = finishedCompetition(2, LocalDate.now().minusDays(2));
        when(competitionService.findCompetitionsWithNotFilledEvents()).thenReturn(List.of(old, recent));

        updater.checkNotFilledEvents();

        verify(updateQueue, never()).requestUpdate(1L);
        verify(updateQueue).requestUpdate(2L);
    }

    // ---- every 20 minutes: closest, not finished competitions ----

    @Test
    void closestCompetitions_ignoreOldCompetitionsWithStuckStatus() {
        LocalDate today = LocalDate.now();
        CompetitionEntity stuckOld = competition(1, today.minusYears(1), today.minusYears(1).plusDays(1), "Заплановано");
        CompetitionEntity upcoming = competition(2, today.plusDays(3), today.plusDays(4), "Відбудеться");
        CompetitionEntity inProgress = competition(3, today.minusDays(1), today.plusDays(1), "Проводиться");
        CompetitionEntity finished = finishedCompetition(4, today.minusDays(2));
        when(competitionService.findAllOrderByBeginDateDesc()).thenReturn(List.of(stuckOld, upcoming, inProgress, finished));

        updater.updateClosestCompetitionDetails();

        verify(updateQueue, never()).requestUpdate(1L);
        verify(updateQueue).requestUpdate(2L);
        verify(updateQueue).requestUpdate(3L);
        verify(updateQueue, never()).requestUpdate(4L);
    }

    // ---- url checks ----

    @Test
    void competitionsWithoutValidUrl_areNotQueued() {
        CompetitionEntity noUrl = finishedCompetition(1, LocalDate.now().minusDays(1));
        noUrl.setUrl("");
        CompetitionEntity pdf = finishedCompetition(2, LocalDate.now().minusDays(1));
        pdf.setUrl("https://example.com/file.pdf");
        when(competitionService.findAllOrderByUpdated()).thenReturn(List.of(noUrl, pdf));
        when(competitionService.findFilledCompetitionIds()).thenReturn(Set.of());

        updater.updateAllCompetitionsDetails();

        verify(updateQueue, never()).requestUpdate(anyLong());
    }
}

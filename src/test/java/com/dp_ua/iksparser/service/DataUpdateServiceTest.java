package com.dp_ua.iksparser.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.entity.DayEntity;
import com.dp_ua.iksparser.dba.service.CompetitionService;
import com.dp_ua.iksparser.dba.service.DayService;
import com.dp_ua.iksparser.dba.service.EventService;
import com.dp_ua.iksparser.dba.service.HeatService;
import com.dp_ua.iksparser.service.parser.CompetitionPageParser;
import com.dp_ua.iksparser.service.parser.EventPageParser;

class DataUpdateServiceTest {

    private CompetitionService competitionService;
    private Downloader downloader;
    private CompetitionPageParser competitionParser;
    private DataUpdateService service;

    @BeforeEach
    void setUp() {
        competitionService = mock(CompetitionService.class);
        downloader = mock(Downloader.class);
        competitionParser = mock(CompetitionPageParser.class);
        service = new DataUpdateService(competitionService, downloader, competitionParser,
                mock(EventPageParser.class), mock(ApplicationEventPublisher.class), mock(DayService.class),
                mock(EventService.class), mock(HeatService.class));
    }

    private CompetitionEntity competition() {
        CompetitionEntity c = new CompetitionEntity();
        c.setId(5L);
        c.setUrl("https://example.com/c5");
        return c;
    }

    @Test
    void update_unknownCompetition_doesNothing() {
        when(competitionService.findById(5L)).thenReturn(null);

        service.update(5L);

        verify(competitionService, never()).save(any());
    }

    @Test
    void update_failure_countsFailedDayOncePerDay() throws Throwable {
        CompetitionEntity competition = competition();
        when(competitionService.findById(5L)).thenReturn(competition);
        when(downloader.getDocument(anyString())).thenThrow(new RuntimeException("site is down"));

        service.update(5L);
        service.update(5L);

        assertEquals(1, competition.getFailedUpdateDays());
        assertEquals(LocalDate.now(), competition.getLastFailedUpdateDate());
    }

    @Test
    void update_pageWithoutDays_countsAsFailedDay() throws Throwable {
        CompetitionEntity competition = competition();
        when(competitionService.findById(5L)).thenReturn(competition);
        when(downloader.getDocument(anyString())).thenReturn(mock(Document.class));
        when(competitionParser.getUnsavedUnfilledDays(any())).thenReturn(List.of());

        service.update(5L);

        assertEquals(1, competition.getFailedUpdateDays());
    }

    @Test
    void update_pageWithDays_resetsFailedDays() throws Throwable {
        CompetitionEntity competition = competition();
        competition.registerUpdateFailure(LocalDate.now().minusDays(3));
        when(competitionService.findById(5L)).thenReturn(competition);
        when(downloader.getDocument(anyString())).thenReturn(mock(Document.class));
        when(competitionParser.getUnsavedUnfilledDays(any()))
                .thenReturn(List.of(new DayEntity("10.11.2026", "101126", "Tue", "Tue")));

        service.update(5L);

        assertEquals(0, competition.getFailedUpdateDays());
    }
}

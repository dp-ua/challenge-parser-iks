package com.dp_ua.iksparser.service.cron;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import com.dp_ua.iksparser.bot.abilities.competition.CompetitionFacade;
import com.dp_ua.iksparser.bot.event.UpdateCompetitionEvent;
import com.dp_ua.iksparser.configuration.CompetitionProperties;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.service.CompetitionService;
import com.dp_ua.iksparser.dba.service.EventService;
import com.dp_ua.iksparser.service.StaleCompetitionPolicy;

class CronCompetitionUpdaterStaleTest {

    private CompetitionService competitionService;
    private ApplicationEventPublisher publisher;
    private CronCompetitionUpdater updater;

    @BeforeEach
    void setUp() {
        competitionService = mock(CompetitionService.class);
        publisher = mock(ApplicationEventPublisher.class);
        StaleCompetitionPolicy policy = new StaleCompetitionPolicy(new CompetitionProperties());
        updater = new CronCompetitionUpdater(mock(EventService.class), competitionService,
                mock(CompetitionFacade.class), publisher, policy);
    }

    private CompetitionEntity competition(long id, LocalDate endDate) {
        CompetitionEntity c = new CompetitionEntity();
        c.setId(id);
        c.setFailedUpdateDays(20);
        c.setUrl("https://example.com/c" + id);
        c.setEndDate(endDate.format(com.dp_ua.iksparser.configuration.Constants.FORMATTER));
        return c;
    }

    @Test
    void dailyUpdate_skipsStaleCompetitions() {
        CompetitionEntity stale = competition(1, LocalDate.now().minusMonths(4));
        CompetitionEntity fresh = competition(2, LocalDate.now().minusDays(5));
        when(competitionService.findAllOrderByUpdated()).thenReturn(List.of(stale, fresh));

        updater.updateAllCompetitionsDetails();

        ArgumentCaptor<UpdateCompetitionEvent> captor = ArgumentCaptor.forClass(UpdateCompetitionEvent.class);
        verify(publisher, times(1)).publishEvent(captor.capture());
        assertEquals(2L, captor.getValue().getMessage().getCompetitionId());
    }
}

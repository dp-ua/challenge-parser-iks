package com.dp_ua.iksparser.dba;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.dp_ua.iksparser.App;
import com.dp_ua.iksparser.bot.abilities.subscribe.SubscribeFacade;
import com.dp_ua.iksparser.bot.command.CommandProvider;
import com.dp_ua.iksparser.bot.event.SendMessageEvent;
import com.dp_ua.iksparser.bot.performer.SendMessagePerformer;
import com.dp_ua.iksparser.dba.entity.CoachEntity;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.entity.DayEntity;
import com.dp_ua.iksparser.dba.entity.EventEntity;
import com.dp_ua.iksparser.dba.entity.HeatEntity;
import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import com.dp_ua.iksparser.dba.service.CoachService;
import com.dp_ua.iksparser.dba.service.CompetitionService;
import com.dp_ua.iksparser.dba.service.DayService;
import com.dp_ua.iksparser.dba.service.EventService;
import com.dp_ua.iksparser.dba.service.HeatLineService;
import com.dp_ua.iksparser.dba.service.HeatService;
import com.dp_ua.iksparser.dba.service.ParticipantService;
import com.dp_ua.iksparser.service.cron.CronCompetitionUpdater;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ComplexDBATest {
    @MockBean
    App app;
    @MockBean
    CommandProvider commandProvider;
    @MockBean
    SendMessagePerformer sendMessagePerformer;
    @MockBean
    CronCompetitionUpdater cronCompetitionUpdater;

    @Autowired
    private CompetitionService competitionService;
    @Autowired
    private DayService dayService;
    @Autowired
    private EventService eventService;
    @Autowired
    private HeatService heatService;
    @Autowired
    private HeatLineService heatLineService;
    @Autowired
    private ParticipantService participantService;
    @Autowired
    private CoachService coachService;
    @Autowired
    SubscribeFacade subscribeFacade;

    @Test
    public void shouldInformAboutNewEvents() {
        fillDB();
        HeatLineEntity heatLine = heatLineService.findAll().iterator().next();
        ParticipantEntity participant = heatLine.getParticipant();

        subscribeFacade.operateParticipantWithHeatlines(participant, List.of(heatLine));
        ArgumentCaptor<SendMessageEvent> captor = ArgumentCaptor.forClass(SendMessageEvent.class);

        verify(sendMessagePerformer, times(1)).onApplicationEvent(captor.capture());
        SendMessage message = (SendMessage) captor.getValue().getMessage();
        Assert.assertEquals("""
                \uD83D\uDD14Ви підписані на учасника:\s
                
                [\uD83C\uDFC3surname firstName ](url) born\uD83C\uDF82
                   \uD83D\uDDFA️region, team
                Приймає участь у змаганнях: _name_
                  \uD83D\uDCC5_ Дата: _beginDate - endDate
                  \uD83D\uDDFA️_ Місце проведення: _country, *city*  [посилання\uD83C\uDF10](url)
                
                Є результати змагання:
                \uD83C\uDFF7️ dayName, time, [eventName, category, round](resultUrl) [\uD83D\uDCCA](resultUrl), heatName, д.1,bib.bib
                
                """, message.getText());
    }

    @Test
    public void shouldGetCompetitionByHeatLine() {

        fillDB();
        // given
        HeatLineEntity heatLine = heatLineService.findAll().iterator().next();

        // when
        Optional<CompetitionEntity> competition = competitionService.getCompetitionByHeatLine(heatLine);

        // then
        assertThat(competition).isPresent();
        assertThat(competition.get().getBeginDate()).isEqualTo("beginDate");
    }

    @Test
    @Transactional
    public void shouldSaveAndLoadFullCompetition_withEntities() {
        fillDB();
        // when load competition by name
        CompetitionEntity competitionFromDb = competitionService.findByName("name");

        // then
        assertThat(competitionFromDb).isNotNull();
        assertThat(competitionFromDb.getName()).isEqualTo("name");
        assertThat(competitionFromDb.getBeginDate()).isEqualTo("beginDate");
        assertThat(competitionFromDb.getEndDate()).isEqualTo("endDate");
        assertThat(competitionFromDb.getCountry()).isEqualTo("country");
        assertThat(competitionFromDb.getCity()).isEqualTo("city");
        assertThat(competitionFromDb.getUrl()).isEqualTo("url");

        List<DayEntity> daysFromDb = competitionFromDb.getDays();
        assertThat(daysFromDb).hasSize(1);

        DayEntity dayFromDb = daysFromDb.get(0);
        assertThat(dayFromDb.getDate()).isEqualTo("date");
        assertThat(dayFromDb.getDateId()).isEqualTo("dateId");
        assertThat(dayFromDb.getDayName()).isEqualTo("dayName");
        assertThat(dayFromDb.getDayNameEn()).isEqualTo("dayNameEn");
        assertThat(dayFromDb.getCompetition().getName()).isEqualTo("name");

        List<EventEntity> eventsFromDb = dayFromDb.getEvents();
        assertThat(eventsFromDb).hasSize(1);

        EventEntity eventFromDb = eventsFromDb.get(0);
        assertThat(eventFromDb.getTime()).isEqualTo("time");
        assertThat(eventFromDb.getEventName()).isEqualTo("eventName");
        assertThat(eventFromDb.getCategory()).isEqualTo("category");
        assertThat(eventFromDb.getRound()).isEqualTo("round");
        assertThat(eventFromDb.getStartListUrl()).isEqualTo("startListUrl");
        assertThat(eventFromDb.getDay().getDayName()).isEqualTo("dayName");

        List<HeatEntity> heatsFromDb = eventFromDb.getHeats();
        assertThat(heatsFromDb).hasSize(1);

        HeatEntity heatFromDb = heatsFromDb.get(0);
        assertThat(heatFromDb.getName()).isEqualTo("heatName");
        assertThat(heatFromDb.getEvent().getEventName()).isEqualTo("eventName");

        List<HeatLineEntity> heatLinesFromDb = heatFromDb.getHeatLines();
        assertThat(heatLinesFromDb).hasSize(1);

        HeatLineEntity heatLineFromDb = heatLinesFromDb.get(0);
        assertThat(heatLineFromDb.getLane()).isEqualTo("1");
        assertThat(heatLineFromDb.getBib()).isEqualTo("bib");
        assertThat(heatLineFromDb.getHeat().getName()).isEqualTo("heatName");

        ParticipantEntity participantFromDb = heatLineFromDb.getParticipant();
        assertThat(participantFromDb.getSurname()).isEqualTo("surname");
        assertThat(participantFromDb.getName()).isEqualTo("firstName");
        assertThat(participantFromDb.getTeam()).isEqualTo("team");
        assertThat(participantFromDb.getRegion()).isEqualTo("region");
        assertThat(participantFromDb.getBorn()).isEqualTo("born");
        assertThat(participantFromDb.getUrl()).isEqualTo("url");
        assertThat(participantFromDb.getHeatLines()).hasSize(1);

        participantFromDb.getHeatLines().forEach(heatLineEntity -> {
            assertThat(heatLineEntity.getLane()).isEqualTo("1");
            assertThat(heatLineEntity.getBib()).isEqualTo("bib");
        });

        List<CoachEntity> coaches = heatLineFromDb.getCoaches();
        assertThat(coaches).hasSize(1);
        CoachEntity coachFromDb = coaches.get(0);

        assertThat(coachFromDb.getName()).isEqualTo("coachName");
        assertThat(coachFromDb.getHeatLines()).hasSize(1);
        coachFromDb.getHeatLines().forEach(heatLineEntity -> {
            assertThat(heatLineEntity.getLane()).isEqualTo("1");
            assertThat(heatLineEntity.getBib()).isEqualTo("bib");
        });
    }

    private void fillDB() {
        CompetitionEntity competition = getCompetition();
        DayEntity day = getDay();
        competition.addDay(day);
        day.setCompetition(competition);

        EventEntity event = getEvent();
        day.addEvent(event);
        event.setDay(day);

        HeatEntity heat = getHeatEntity();
        event.addHeat(heat);
        heat.setEvent(event);

        HeatLineEntity heatLine = getHeatLineEntity(heat);
        log.warn("heatLine: {}", heatLine.getId());
        heat.addHeatLine(heatLine);
        heatLine.setHeat(heat);

        ParticipantEntity participant = getParticipantEntity();
        heatLine.setParticipant(participant);
        participant.addHeatLine(heatLine);

        CoachEntity coach = getCoachEntity();
        heatLine.addCoach(coach);
        coach.addHeatLine(heatLine);

        competitionService.save(competition);
        competitionService.flush();

        subscribeFacade.subscribe("chatId", participant);
    }

    private CoachEntity getCoachEntity() {
        CoachEntity coach = new CoachEntity();
        coach.setName("coachName");
        return coachService.save(coach);
    }

    private HeatLineEntity getHeatLineEntity(HeatEntity heat) {
        HeatLineEntity heatLine = new HeatLineEntity();
        heatLine.setHeat(heat);
        heatLine.setLane("1");
        heatLine.setBib("bib");
        return heatLineService.save(heatLine);
    }

    private ParticipantEntity getParticipantEntity() {
        ParticipantEntity participant = new ParticipantEntity();
        participant.setSurname("surname");
        participant.setName("firstName");
        participant.setRegion("region");
        participant.setTeam("team");
        participant.setBorn("born");
        participant.setUrl("url");
        return participantService.save(participant);
    }

    private HeatEntity getHeatEntity() {
        HeatEntity heat = new HeatEntity();
        heat.setName("heatName");
        return heatService.save(heat);
    }

    private EventEntity getEvent() {
        EventEntity event = new EventEntity("time", "eventName", "category", "round", "startListUrl", "resultUrl");
        return eventService.save(event);
    }

    private DayEntity getDay() {
        DayEntity day = new DayEntity("date", "dateId", "dayName", "dayNameEn");
        return dayService.save(day);
    }

    private CompetitionEntity getCompetition() {
        CompetitionEntity competition = new CompetitionEntity();
        competition.setName("name");
        competition.setBeginDate("beginDate");
        competition.setEndDate("endDate");
        competition.setCountry("country");
        competition.setCity("city");
        competition.setUrl("url");
        return competitionService.save(competition);
    }

}

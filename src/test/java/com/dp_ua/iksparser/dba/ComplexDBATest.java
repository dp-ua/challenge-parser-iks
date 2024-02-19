package com.dp_ua.iksparser.dba;

import com.dp_ua.iksparser.App;
import com.dp_ua.iksparser.bot.command.CommandProvider;
import com.dp_ua.iksparser.dba.element.*;
import com.dp_ua.iksparser.dba.service.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ComplexDBATest {
    @MockBean
    App app;
    @MockBean
    CommandProvider commandProvider;

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

    @Test
    @Transactional
    public void shouldSaveAndLoadFullCompetition_withEntities() {
        // given . Create full competition with all entities
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

        // when load competition by name
        CompetitionEntity competitionFromDb = competitionService.findByName("name");

        // then
        assert competitionFromDb != null;
        assert competitionFromDb.getName().equals("name");
        assert competitionFromDb.getBeginDate().equals("beginDate");
        assert competitionFromDb.getEndDate().equals("endDate");
        assert competitionFromDb.getCountry().equals("country");
        assert competitionFromDb.getCity().equals("city");
        assert competitionFromDb.getUrl().equals("url");

        List<DayEntity> daysFromDb = competitionFromDb.getDays();
        assert daysFromDb.size() == 1;
        DayEntity dayFromDb = daysFromDb.get(0);
        assert dayFromDb.getDate().equals("date");
        assert dayFromDb.getDateId().equals("dateId");
        assert dayFromDb.getDayName().equals("dayName");
        assert dayFromDb.getDayNameEn().equals("dayNameEn");
        assert dayFromDb.getCompetition().getName().equals("name");

        List<EventEntity> eventsFromDb = dayFromDb.getEvents();
        assert eventsFromDb.size() == 1;
        EventEntity eventFromDb = eventsFromDb.get(0);
        assert eventFromDb.getTime().equals("time");
        assert eventFromDb.getEventName().equals("eventName");
        assert eventFromDb.getCategory().equals("category");
        assert eventFromDb.getRound().equals("round");
        assert eventFromDb.getStartListUrl().equals("startListUrl");
        assert eventFromDb.getDay().getDayName().equals("dayName");

        List<HeatEntity> heatsFromDb = eventFromDb.getHeats();
        assert heatsFromDb.size() == 1;
        HeatEntity heatFromDb = heatsFromDb.get(0);
        assert heatFromDb.getName().equals("heatName");
        assert heatFromDb.getEvent().getEventName().equals("eventName");

        List<HeatLineEntity> heatLinesFromDb = heatFromDb.getHeatLines();
        assert heatLinesFromDb.size() == 1;
        HeatLineEntity heatLineFromDb = heatLinesFromDb.get(0);
        assert heatLineFromDb.getLane().equals("1");
        assert heatLineFromDb.getBib().equals("bib");
        assert heatLineFromDb.getHeat().getName().equals("heatName");

        ParticipantEntity participantFromDb = heatLineFromDb.getParticipant();
        assert participantFromDb.getSurname().equals("surname");
        assert participantFromDb.getName().equals("firstName");
        assert participantFromDb.getTeam().equals("team");
        assert participantFromDb.getRegion().equals("region");
        assert participantFromDb.getBorn().equals("born");
        assert participantFromDb.getUrl().equals("url");
        assert participantFromDb.getHeatLines().size() == 1;
        participantFromDb.getHeatLines().forEach(heatLineEntity -> {
            assert heatLineEntity.getLane().equals("1");
            assert heatLineEntity.getBib().equals("bib");
        });

        assert participantFromDb.getHeatLines().size() == 1;
        participantFromDb.getHeatLines().forEach(heatLineEntity -> {
            assert heatLineEntity.getLane().equals("1");
            assert heatLineEntity.getBib().equals("bib");
        });

        List<CoachEntity> coaches = heatLineFromDb.getCoaches();
        assert coaches.size() == 1;
        CoachEntity coachFromDb = coaches.get(0);
        assert coachFromDb.getName().equals("coachName");
        assert coachFromDb.getHeatLines().size() == 1;
        coachFromDb.getHeatLines().forEach(heatLineEntity -> {
            assert heatLineEntity.getLane().equals("1");
            assert heatLineEntity.getBib().equals("bib");
        });
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

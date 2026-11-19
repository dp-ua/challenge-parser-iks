package com.dp_ua.iksparser.dba.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.entity.DayEntity;
import com.dp_ua.iksparser.dba.entity.EventEntity;

@DataJpaTest
class CompetitionRepoNotFilledEventsTest {

    @Autowired
    private CompetitionRepo repo;

    private EventEntity event(String startList, String result) {
        EventEntity event = new EventEntity();
        event.setEventName("100m");
        event.setStartListUrl(startList);
        event.setResultUrl(result);
        return event;
    }

    private CompetitionEntity saveCompetition(String name, EventEntity... events) {
        CompetitionEntity competition = new CompetitionEntity();
        competition.setName(name);
        competition.setUrl("http://test.com/" + name);
        DayEntity day = new DayEntity("10.11.2026", "101126", "Tue", "Tue");
        day.setCompetition(competition);
        competition.addDay(day);
        for (EventEntity event : events) {
            event.setDay(day);
            day.addEvent(event);
        }
        CompetitionEntity saved = repo.save(competition);
        repo.flush();
        return saved;
    }

    @Test
    void returnsCompetitionWithEventWithoutAnyLinks() {
        CompetitionEntity expected = saveCompetition("unfilled", event("", ""));

        List<CompetitionEntity> result = repo.findAllWithNotFilledEvents();

        assertEquals(List.of(expected.getId()), result.stream().map(CompetitionEntity::getId).toList());
    }

    @Test
    void treatsNullLinksAsEmpty() {
        CompetitionEntity expected = saveCompetition("null links", event(null, null));

        List<CompetitionEntity> result = repo.findAllWithNotFilledEvents();

        assertEquals(List.of(expected.getId()), result.stream().map(CompetitionEntity::getId).toList());
    }

    @Test
    void ignoresCompetitionWhereEveryEventHasAStartListOrResult() {
        saveCompetition("filled", event("http://start", ""), event("", "http://result"), event("http://s", "http://r"));

        assertEquals(0, repo.findAllWithNotFilledEvents().size());
    }

    @Test
    void returnsCompetitionOnlyOnce_whenSeveralEventsAreNotFilled() {
        CompetitionEntity expected = saveCompetition("mixed",
                event("", ""), event("", ""), event("http://start", ""));

        List<CompetitionEntity> result = repo.findAllWithNotFilledEvents();

        assertEquals(List.of(expected.getId()), result.stream().map(CompetitionEntity::getId).toList());
    }

    @Test
    void ignoresCompetitionWithoutEvents() {
        saveCompetition("no events");

        assertEquals(0, repo.findAllWithNotFilledEvents().size());
    }
}

package com.dp_ua.iksparser.dba.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.dp_ua.iksparser.App;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class CompetitionRepoTest {

    @Autowired
    private CompetitionRepo repo;

    @MockBean
    App app;

    @Test
    void shouldSaveAndRetrieveCompetition() {
        // given
        var competition = new CompetitionEntity();
        var expectedName = "Test competition";
        var expectedBegin = LocalDate.of(2020, 1, 1).toString();
        var expectedEnd = LocalDate.of(2020, 1, 2).toString();
        var expectedUrl = "https://test.com";

        competition.setName(expectedName);
        competition.setBeginDate(expectedBegin);
        competition.setEndDate(expectedEnd);
        competition.setUrl(expectedUrl);
        log.info("competition before save: {}", competition);

        // when
        var savedCompetition = repo.save(competition);
        log.info("competition after save: {}", savedCompetition);

        // then
        var competitionFromDb = repo.findByName("Test competition");
        log.info("competitionFromDb: {}", competitionFromDb);

        assertNotNull(competitionFromDb);
        assertEquals(expectedName, competitionFromDb.getName());
        assertEquals(expectedBegin, competitionFromDb.getBeginDate());
        assertEquals(expectedEnd, competitionFromDb.getEndDate());
        assertEquals(expectedUrl, competitionFromDb.getUrl());
    }

}

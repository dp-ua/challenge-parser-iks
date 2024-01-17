package com.dp_ua.iksparser.dba;

import com.dp_ua.iksparser.MockBotControllerTest;
import com.dp_ua.iksparser.element.Competition;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

//@DataJpaTest
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class CompetitionRepoTest extends MockBotControllerTest {
    @Autowired
    private CompetitionRepo repo;

    @Test
    public void shouldSaveAndRetrieveCompetition() {
// given
        Competition competition = new Competition();
        String expectedName = "Test competition";
        String expectedBegin = LocalDate.of(2020, 1, 1).toString();
        String expectedEnd = LocalDate.of(2020, 1, 2).toString();
        String expectedUrl = "https://test.com";

        competition.setName(expectedName);
        competition.setBeginDate(expectedBegin);
        competition.setEndDate(expectedEnd);
        competition.setUrl(expectedUrl);
        log.info("competition before save: " + competition);

        // when
        Competition savedCompetition = repo.save(competition);
        log.info("competition after save: " + savedCompetition);

        // then
        Competition competitionFromDb = repo.findByName("Test competition");
        log.info("competitionFromDb: " + competitionFromDb);

        assert competitionFromDb != null;
        assert competitionFromDb.getName().equals(expectedName);
        assert competitionFromDb.getBeginDate().equals(expectedBegin);
        assert competitionFromDb.getEndDate().equals(expectedEnd);
        assert competitionFromDb.getUrl().equals(expectedUrl);
    }

    @Override
    public void additionalSetUp() {
        // nothing to do
    }
}
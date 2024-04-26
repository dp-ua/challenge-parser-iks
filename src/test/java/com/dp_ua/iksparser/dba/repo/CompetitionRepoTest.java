package com.dp_ua.iksparser.dba.repo;

import com.dp_ua.iksparser.App;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

//@DataJpaTest
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class CompetitionRepoTest {
    @Autowired
    private CompetitionRepo repo;
    @MockBean
    App app;

    @Test
    public void shouldSaveAndRetrieveCompetition() {
// given
        CompetitionEntity competition = new CompetitionEntity();
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
        CompetitionEntity savedCompetition = repo.save(competition);
        log.info("competition after save: " + savedCompetition);

        // then
        CompetitionEntity competitionFromDb = repo.findByName("Test competition");
        log.info("competitionFromDb: " + competitionFromDb);

        assert competitionFromDb != null;
        assert competitionFromDb.getName().equals(expectedName);
        assert competitionFromDb.getBeginDate().equals(expectedBegin);
        assert competitionFromDb.getEndDate().equals(expectedEnd);
        assert competitionFromDb.getUrl().equals(expectedUrl);
    }
}
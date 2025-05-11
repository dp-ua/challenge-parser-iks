package com.dp_ua.iksparser.dba.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;

import com.dp_ua.iksparser.dba.dto.CompetitionDto;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.repo.CompetitionRepo;
import com.dp_ua.iksparser.service.PageableService;
import com.dp_ua.iksparser.service.SqlPreprocessorService;

@DataJpaTest
class CompetitionServiceTest {
    @Autowired
    CompetitionService service;

    @Autowired
    CompetitionRepo repo;

    @Test
    void shouldLoadCompetition_All() {
        Iterable<CompetitionEntity> all = service.findAllOrderByBeginDateDesc();
        assertEquals(4, all.spliterator().getExactSizeIfKnown());
    }

    @Test
    void shouldLoadCompetition_FirstPage_Limit2() {
        Page<CompetitionDto> allCompetitions = service.getCompetitions(null, null, 0, 2);
        assertEquals(2, allCompetitions.getContent().size());
    }

    @Test
    void shouldLoadCompetition_SecondPage_Limit3() {
        Page<CompetitionDto> allCompetitions = service.getCompetitions(null, null, 1, 3);
        assertEquals(1, allCompetitions.getContent().size());
        assertEquals("Test competition alfa", allCompetitions.getContent().get(0).getName());
    }

    @Test
    void shouldLoadCompetition_OnlyOne_TextInput() {
        Page<CompetitionDto> allCompetitions = service.getCompetitions("alfa", null, 0, 4);
        assertEquals(1, allCompetitions.getContent().size());
        assertEquals("Test competition alfa", allCompetitions.getContent().get(0).getName());
    }

    @Test
    void shouldLoadCompetitions_Matched_All() {
        Page<CompetitionDto> allCompetitions = service.getCompetitions("Test", null, 0, 4);
        List<CompetitionDto> content = allCompetitions.getContent();
        assertEquals(4, content.size());
        assertEquals("Test competition delta", content.get(0).getName());
        assertEquals("Test competition gamma", content.get(1).getName());
        assertEquals("Test competition beta", content.get(2).getName());
        assertEquals("Test competition alfa", content.get(3).getName());
    }

    @Test
    void shouldLoadCompetition_OnlyOne_TextInput_IgnoreCase() {
        Page<CompetitionDto> allCompetitions = service.getCompetitions("Alfa", null, 0, 4);
        assertEquals(1, allCompetitions.getContent().size());
        assertEquals("Test competition alfa", allCompetitions.getContent().get(0).getName());
    }

    @Test
    void shouldLoadCompetition_OnlyOne_TextInputTwoWorlds_IgnoreCase() {
        Page<CompetitionDto> allCompetitions = service.getCompetitions("Alfa test", null, 0, 4);
        assertEquals(1, allCompetitions.getContent().size());
        assertEquals("Test competition alfa", allCompetitions.getContent().get(0).getName());
    }

    @BeforeEach
    void setUp() {
        System.out.println("Before each");
        prepareCompetitionRepo();
    }

    private void prepareCompetitionRepo() {
        CompetitionEntity competitionEntity = new CompetitionEntity();
        competitionEntity.setName("Test competition alfa");
        competitionEntity.setBeginDate("01.01.2021");
        competitionEntity.setUrl("http://test.com");
        repo.save(competitionEntity);

        competitionEntity = new CompetitionEntity();
        competitionEntity.setName("Test competition beta");
        competitionEntity.setBeginDate("01.01.2022");
        competitionEntity.setUrl("http://test.com");
        repo.save(competitionEntity);

        competitionEntity = new CompetitionEntity();
        competitionEntity.setName("Test competition gamma");
        competitionEntity.setBeginDate("01.01.2023");
        competitionEntity.setUrl("http://test.com");
        repo.save(competitionEntity);

        competitionEntity = new CompetitionEntity();
        competitionEntity.setName("Test competition delta");
        competitionEntity.setBeginDate("01.01.2024");
        competitionEntity.setUrl("http://test.com");
        repo.save(competitionEntity);
    }

    @TestConfiguration
    static class CompetitionServiceTestContextConfiguration {

        @Bean
        public SqlPreprocessorService sqlPreprocessorService() {
            return new SqlPreprocessorService();
        }

        @Bean
        public CompetitionService competitionService(CompetitionRepo repo,
                                                     SqlPreprocessorService sqlPreprocessorService,
                                                     PageableService pageableService) {
            return new CompetitionService(repo, sqlPreprocessorService, pageableService);
        }

        @Bean
        public PageableService pageableService() {
            return new PageableService();
        }
    }
}
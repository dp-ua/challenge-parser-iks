package com.dp_ua.iksparser.dba.service;

import com.dp_ua.iksparser.dba.element.ParticipantEntity;
import com.dp_ua.iksparser.dba.repo.ParticipantRepo;
import com.dp_ua.iksparser.service.SqlPreprocessorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class ParticipantServiceTest {
    @Autowired
    ParticipantService service;

    @BeforeEach
    public void setUp() {
        ParticipantEntity participant1 = new ParticipantEntity();
        participant1.setName("Kolya");
        participant1.setSurname("Targetov");
        service.save(participant1);
        ParticipantEntity participant2 = new ParticipantEntity();
        participant2.setName("Vasya");
        participant2.setSurname("Kotargetanov");
        service.save(participant2);
        ParticipantEntity participant3 = new ParticipantEntity();
        participant3.setName("Vasya");
        participant3.setSurname("Uraganov");
        service.save(participant3);
    }

    @Test
    public void shouldFindAll() {
        Iterable<ParticipantEntity> all = service.findAll();
        assertEquals(3, all.spliterator().getExactSizeIfKnown());
    }

    @Test
    public void shouldFind_OneParticipant_OnlyName() {
        List<ParticipantEntity> found = service.findBySurnameAndNameParts(List.of("Kolya"));
        assertEquals(1, found.size());
        assertEquals("Kolya", found.get(0).getName());
        assertEquals("Targetov", found.get(0).getSurname());
    }

    @Test
    public void shouldFind_OneParticipant_NameAndSurname() {
        List<ParticipantEntity> found = service.findBySurnameAndNameParts(List.of("Kolya", "Targetov"));
        assertEquals(1, found.size());
        assertEquals("Kolya", found.get(0).getName());
        assertEquals("Targetov", found.get(0).getSurname());
    }

    @Test
    public void shouldFind_One_ByNameAndPartOfSurname() {
        List<ParticipantEntity> found = service.findBySurnameAndNameParts(List.of("vasya", "target"));
        assertEquals(1, found.size());
        assertEquals("Vasya", found.get(0).getName());
        assertEquals("Kotargetanov", found.get(0).getSurname());
    }

    @Test
    public void shouldFind_TwoParticipant_ByName() {
        List<ParticipantEntity> found = service.findBySurnameAndNameParts(List.of("Vasya"));
        assertEquals(2, found.size());
    }

    @Test
    public void shouldFind_TwoParticipant_ByPartOfSurname() {
        List<ParticipantEntity> found = service.findBySurnameAndNameParts(List.of("target"));
        assertEquals(2, found.size());
    }

    @TestConfiguration
    static class ParticipantServiceTestContextConfiguration {
        @Bean
        public SqlPreprocessorService sqlPreprocessorService() {
            return new SqlPreprocessorService();
        }

        @Bean
        public ParticipantService participantService(ParticipantRepo participantRepo) {
            return new ParticipantService(participantRepo);
        }
    }
}
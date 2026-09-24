package com.dp_ua.iksparser.dba.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.dp_ua.iksparser.dba.entity.ParticipantEntity;

@DataJpaTest
class ParticipantRepoTest {

    @Autowired
    ParticipantRepo participantRepo;

    @BeforeEach
    void setUp() {
        participantRepo.deleteAll();
        ParticipantEntity participant1 = new ParticipantEntity();
        participant1.setName("Kolya");
        participant1.setSurname("Targetov");
        participantRepo.save(participant1);
        ParticipantEntity participant2 = new ParticipantEntity();
        participant2.setName("Vasya");
        participant2.setSurname("Kotargetanov");
        participantRepo.save(participant2);
        // add 2 duplicates
        ParticipantEntity p3 = new ParticipantEntity();
        p3.setName("Ulya");
        p3.setSurname("Boolya");
        p3.setBorn("2000-01-01");
        participantRepo.save(p3);
        ParticipantEntity p4 = new ParticipantEntity();
        p4.setName("Ulya");
        p4.setSurname("Boolya");
        p4.setBorn("2000-01-01");
        participantRepo.save(p4);
    }

    @Test
    void shouldFindAll() {
        List<ParticipantEntity> found = participantRepo.findAll();
        assertEquals(4, found.size());
    }

    @Test
    void shouldFindDuplicates() {
        List<ParticipantEntity> duplicates = participantRepo.findDuplicates();
        assertEquals(2, duplicates.size());
    }

}
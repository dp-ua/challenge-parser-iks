package com.dp_ua.iksparser.dba.repo;

import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
public class ParticipantRepoTest {
    @Autowired
    ParticipantRepo participantRepo;

    @BeforeEach
    public void setUp() {
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
    public void shouldFindAll() {
        List<ParticipantEntity> found = (List<ParticipantEntity>) participantRepo.findAll();
        assertEquals(4, found.size());
    }

    // should not find any participants
    @Test
    public void shouldFind_None() {
        List<ParticipantEntity> found = participantRepo.findByNameAndSurnameByPart("Ivan");
        Assertions.assertTrue(found.isEmpty());
    }

    @Test
    public void shouldFind_OneParticipant_FullName() {
        List<ParticipantEntity> found = participantRepo.findByNameAndSurnameByPart("Kolya");
        assertFalse(found.isEmpty());
        assertEquals(1, found.size());
        assertEquals("Kolya", found.get(0).getName());
        assertEquals("Targetov", found.get(0).getSurname());
    }

    @Test
    public void shouldFind_OneParticipant_Surname() {
        List<ParticipantEntity> found = participantRepo.findByNameAndSurnameByPart("Targetov");
        assertEquals(1, found.size());
        assertEquals("Kolya", found.get(0).getName());
        assertEquals("Targetov", found.get(0).getSurname());
    }

    @Test
    public void shouldFind_OneParticipant_PartialName() {
        List<ParticipantEntity> found = participantRepo.findByNameAndSurnameByPart("Kol");
        assertEquals(1, found.size());
        assertEquals("Kolya", found.get(0).getName());
        assertEquals("Targetov", found.get(0).getSurname());
    }

    @Test
    public void shouldFind_OneParticipant_IgnoreCase() {
        List<ParticipantEntity> found = participantRepo.findByNameAndSurnameByPart("kolya");
        assertEquals(1, found.size());
        assertEquals("Kolya", found.get(0).getName());
        assertEquals("Targetov", found.get(0).getSurname());
    }

    @Test
    public void shouldFind_TwoParticipants_SurnamePart() {
        List<ParticipantEntity> found = participantRepo.findByNameAndSurnameByPart("target");
        assertEquals(2, found.size());
    }

    @Test
    public void shouldFindDuplicates() {
        List<ParticipantEntity> duplicates = participantRepo.findDuplicates();
        assertEquals(2, duplicates.size());
    }
}
package com.dp_ua.iksparser.dba.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import com.dp_ua.iksparser.dba.repo.ParticipantRepo;
import com.dp_ua.iksparser.service.PageableService;
import com.dp_ua.iksparser.service.SqlPreprocessorService;

@DataJpaTest
@DisplayName("ParticipantService Tests")
class ParticipantServiceTest {

    @Autowired
    private ParticipantService service;

    @BeforeEach
    void setUp() {
        // Arrange - prepare test data
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
    @DisplayName("Should find all participants")
    void shouldFindAll() {
        // Act
        Iterable<ParticipantEntity> all = service.findAll();

        // Assert
        assertEquals(3, all.spliterator().getExactSizeIfKnown());
    }

    @Test
    @DisplayName("Should find one participant by name only")
    void shouldFindOneParticipantByNameOnly() {
        // Act
        List<ParticipantEntity> found = service.findAllBySurnameAndNameParts(List.of("Kolya"));

        // Assert
        assertAll("Should find Kolya Targetov",
                () -> assertEquals(1, found.size()),
                () -> assertNotNull(found.get(0)),
                () -> assertEquals("Kolya", found.get(0).getName()),
                () -> assertEquals("Targetov", found.get(0).getSurname())
        );
    }

    @Test
    @DisplayName("Should find one participant by name and surname")
    void shouldFindOneParticipantByNameAndSurname() {
        // Act
        List<ParticipantEntity> found = service.findAllBySurnameAndNameParts(List.of("Kolya", "Targetov"));

        // Assert
        assertAll("Should find Kolya Targetov",
                () -> assertEquals(1, found.size()),
                () -> assertNotNull(found.get(0)),
                () -> assertEquals("Kolya", found.get(0).getName()),
                () -> assertEquals("Targetov", found.get(0).getSurname())
        );
    }

    @Test
    @DisplayName("Should find one participant by name and part of surname")
    void shouldFindOneParticipantByNameAndPartOfSurname() {
        // Act
        List<ParticipantEntity> found = service.findAllBySurnameAndNameParts(List.of("vasya", "target"));

        // Assert
        assertAll("Should find Vasya Kotargetanov",
                () -> assertEquals(1, found.size(), "Should find exactly one participant"),
                () -> assertNotNull(found.get(0)),
                () -> assertEquals("Vasya", found.get(0).getName()),
                () -> assertEquals("Kotargetanov", found.get(0).getSurname())
        );
    }

    @Test
    @DisplayName("Should find two participants by name")
    void shouldFindTwoParticipantsByName() {
        // Act
        List<ParticipantEntity> found = service.findAllBySurnameAndNameParts(List.of("Vasya"));

        // Assert
        assertAll("Should find two Vasyas",
                () -> assertEquals(2, found.size()),
                () -> assertEquals("Vasya", found.get(0).getName()),
                () -> assertEquals("Vasya", found.get(1).getName())
        );
    }

    @Test
    @DisplayName("Should find two participants by part of surname")
    void shouldFindTwoParticipantsByPartOfSurname() {
        // Act
        List<ParticipantEntity> found = service.findAllBySurnameAndNameParts(List.of("target"));

        // Assert
        assertEquals(2, found.size(), "Should find two participants with 'target' in surname");
    }

    @Test
    @DisplayName("Should handle case-insensitive search")
    void shouldHandleCaseInsensitiveSearch() {
        // Act
        List<ParticipantEntity> foundLowerCase = service.findAllBySurnameAndNameParts(List.of("kolya"));
        List<ParticipantEntity> foundUpperCase = service.findAllBySurnameAndNameParts(List.of("KOLYA"));
        List<ParticipantEntity> foundMixedCase = service.findAllBySurnameAndNameParts(List.of("KoLyA"));

        // Assert
        assertAll("Search should be case-insensitive",
                () -> assertEquals(1, foundLowerCase.size()),
                () -> assertEquals(1, foundUpperCase.size()),
                () -> assertEquals(1, foundMixedCase.size()),
                () -> assertEquals("Kolya", foundLowerCase.get(0).getName()),
                () -> assertEquals("Kolya", foundUpperCase.get(0).getName()),
                () -> assertEquals("Kolya", foundMixedCase.get(0).getName())
        );
    }

    @Test
    @DisplayName("Should return empty list when no match found")
    void shouldReturnEmptyListWhenNoMatchFound() {
        // Act
        List<ParticipantEntity> found = service.findAllBySurnameAndNameParts(List.of("NonExistent"));

        // Assert
        assertNotNull(found, "Result should not be null");
        assertEquals(0, found.size(), "Should return empty list when no match found");
    }

    @Test
    @DisplayName("Should find participant by partial match in both name and surname")
    void shouldFindByPartialMatchInBothNameAndSurname() {
        // Act
        List<ParticipantEntity> found = service.findAllBySurnameAndNameParts(List.of("vas", "urag"));

        // Assert
        assertAll("Should find Vasya Uraganov",
                () -> assertEquals(1, found.size()),
                () -> assertEquals("Vasya", found.get(0).getName()),
                () -> assertEquals("Uraganov", found.get(0).getSurname())
        );
    }

    @TestConfiguration
    static class ParticipantServiceTestContextConfiguration {

        @Bean
        public SqlPreprocessorService sqlPreprocessorService() {
            return new SqlPreprocessorService();
        }

        @Bean
        public ParticipantService participantService(ParticipantRepo participantRepo,
                                                     SqlPreprocessorService sqlPreprocessorService,
                                                     PageableService pageableService) {
            return new ParticipantService(participantRepo, sqlPreprocessorService, pageableService);
        }

        @Bean
        public PageableService pageableService() {
            return new PageableService();
        }

    }

}

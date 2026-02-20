package com.dp_ua.iksparser.dba.service;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.entity.DayEntity;
import com.dp_ua.iksparser.dba.repo.CompetitionRepo;
import com.dp_ua.iksparser.service.PageableService;
import com.dp_ua.iksparser.service.SqlPreprocessorService;

/**
 * Unit tests specifically for the URL change functionality in CompetitionService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CompetitionService URL Change Tests")
class CompetitionServiceUrlChangeTest {

    @Mock
    private CompetitionRepo repo;

    @Mock
    private SqlPreprocessorService sqlPreprocessorService;

    @Mock
    private PageableService pageableService;

    @Mock
    private DayService dayService;

    @InjectMocks
    private CompetitionService competitionService;

    private CompetitionEntity existingCompetition;
    private CompetitionEntity newCompetition;
    private List<DayEntity> days;

    @BeforeEach
    void setUp() {
        // Setup existing competition
        existingCompetition = new CompetitionEntity();
        existingCompetition.setId(1L);
        existingCompetition.setName("Test Competition");
        existingCompetition.setBeginDate("01.01.2023");
        existingCompetition.setEndDate("05.01.2023");
        existingCompetition.setUrl("http://example.com/competition/1");

        // Setup days for existing competition
        days = new ArrayList<>();
        DayEntity day1 = new DayEntity("01.01.2023", "day1", "Day 1", "Day 1 EN");
        day1.setCompetition(existingCompetition);
        DayEntity day2 = new DayEntity("02.01.2023", "day2", "Day 2", "Day 2 EN");
        day2.setCompetition(existingCompetition);
        days.add(day1);
        days.add(day2);

        existingCompetition.setDays(days);

        // Setup new competition with different URL
        newCompetition = new CompetitionEntity();
        newCompetition.setName("Test Competition");
        newCompetition.setBeginDate("01.01.2023");
        newCompetition.setEndDate("05.01.2023");
        newCompetition.setUrl("http://example.com/competition/2"); // Different URL
    }

    @Test
    @DisplayName("Should clear days when URL changes")
    void shouldClearDaysWhenUrlChanges() {
        // Arrange
        when(repo.findByNameAndBeginDateAndEndDate(anyString(), anyString(), anyString()))
                .thenReturn(List.of(existingCompetition));
        when(repo.save(any(CompetitionEntity.class))).thenReturn(existingCompetition);

        // Act
        CompetitionEntity result = competitionService.saveOrUpdate(newCompetition);

        // Assert
        assertAll("URL change should clear days and update URL",
                () -> verify(repo).save(existingCompetition),
                () -> assertTrue(result.getDays().isEmpty(), "Days should be cleared when URL changes"),
                () -> assertEquals(newCompetition.getUrl(), result.getUrl(), "URL should be updated")
        );
    }

    @Test
    @DisplayName("Should not clear days when URL does not change")
    void shouldNotClearDaysWhenUrlDoesNotChange() {
        // Arrange
        newCompetition.setUrl(existingCompetition.getUrl()); // Same URL

        when(repo.findByNameAndBeginDateAndEndDate(anyString(), anyString(), anyString()))
                .thenReturn(List.of(existingCompetition));
        when(repo.save(any(CompetitionEntity.class))).thenReturn(existingCompetition);

        // Act
        CompetitionEntity result = competitionService.saveOrUpdate(newCompetition);

        // Assert
        assertAll("Same URL should not clear days",
                () -> verify(repo).save(existingCompetition),
                () -> assertEquals(2, result.getDays().size(), "Days should not be cleared when URL doesn't change")
        );
    }

    @Test
    @DisplayName("Should create new competition when not found")
    void shouldCreateNewCompetitionWhenNotFound() {
        // Arrange
        when(repo.findByNameAndBeginDateAndEndDate(anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        when(repo.save(any(CompetitionEntity.class))).thenReturn(newCompetition);

        // Act
        CompetitionEntity result = competitionService.saveOrUpdate(newCompetition);

        // Assert
        assertAll("New competition should be created",
                () -> verify(repo).save(newCompetition),
                () -> assertEquals(newCompetition, result)
        );
    }

    @Test
    @DisplayName("Should clear days when URL changes from null to non-null")
    void shouldClearDaysWhenUrlChangesFromNullToNonNull() {
        // Arrange - existing competition with null URL
        existingCompetition.setUrl(null);
        newCompetition.setUrl("http://example.com/competition/new");

        when(repo.findByNameAndBeginDateAndEndDate(anyString(), anyString(), anyString()))
                .thenReturn(List.of(existingCompetition));
        when(repo.save(any(CompetitionEntity.class))).thenReturn(existingCompetition);

        // Act
        CompetitionEntity result = competitionService.saveOrUpdate(newCompetition);

        // Assert
        assertAll("URL change from null to non-null should clear days",
                () -> verify(repo).save(existingCompetition),
                () -> assertTrue(result.getDays().isEmpty(), "Days should be cleared when URL changes from null to non-null"),
                () -> assertEquals(newCompetition.getUrl(), result.getUrl(), "URL should be updated")
        );
    }

    @Test
    @DisplayName("Should clear days when URL changes from non-null to null")
    void shouldClearDaysWhenUrlChangesFromNonNullToNull() {
        // Arrange - existing competition with URL, new has null
        existingCompetition.setUrl("http://example.com/competition/old");
        existingCompetition.setDays(new ArrayList<>(days)); // Fresh copy of days
        newCompetition.setUrl(null);

        when(repo.findByNameAndBeginDateAndEndDate(anyString(), anyString(), anyString()))
                .thenReturn(List.of(existingCompetition));
        when(repo.save(any(CompetitionEntity.class))).thenReturn(existingCompetition);

        // Act
        CompetitionEntity result = competitionService.saveOrUpdate(newCompetition);

        // Assert
        assertAll("URL change from non-null to null should clear days",
                () -> verify(repo).save(existingCompetition),
                () -> assertTrue(result.getDays().isEmpty(), "Days should be cleared when URL changes from non-null to null"),
                () -> assertEquals(EMPTY, result.getUrl(), "URL should be updated to null and returned as EMPTY")
        );
    }

    @Test
    @DisplayName("Should not clear days when both URLs are null")
    void shouldNotClearDaysWhenBothUrlsAreNull() {
        // Arrange
        existingCompetition.setUrl(null);
        newCompetition.setUrl(null);

        when(repo.findByNameAndBeginDateAndEndDate(anyString(), anyString(), anyString()))
                .thenReturn(List.of(existingCompetition));
        when(repo.save(any(CompetitionEntity.class))).thenReturn(existingCompetition);

        // Act
        CompetitionEntity result = competitionService.saveOrUpdate(newCompetition);

        // Assert
        assertAll("Both URLs null should not clear days",
                () -> verify(repo).save(existingCompetition),
                () -> assertEquals(2, result.getDays().size(), "Days should not be cleared when both URLs are null"),
                () -> assertEquals(EMPTY, result.getUrl(), "URL should remain null and  returned as EMPTY")
        );
    }

    @Test
    @DisplayName("Should not clear days when both URLs are empty strings")
    void shouldNotClearDaysWhenBothUrlsAreEmpty() {
        // Arrange
        existingCompetition.setUrl("");
        newCompetition.setUrl("");

        when(repo.findByNameAndBeginDateAndEndDate(anyString(), anyString(), anyString()))
                .thenReturn(List.of(existingCompetition));
        when(repo.save(any(CompetitionEntity.class))).thenReturn(existingCompetition);

        // Act
        CompetitionEntity result = competitionService.saveOrUpdate(newCompetition);

        // Assert
        assertAll("Both URLs empty should not clear days",
                () -> verify(repo).save(existingCompetition),
                () -> assertEquals(2, result.getDays().size(), "Days should not be cleared when both URLs are empty"),
                () -> assertEquals(EMPTY, result.getUrl(), "URL should be EMPTY (empty strings are converted to EMPTY)")  // ✅ Изменено
        );
    }

}

package com.dp_ua.iksparser.dba.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
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
    void shouldClearDaysWhenUrlChanges() {
        // Arrange
        when(repo.findByNameAndBeginDateAndEndDate(anyString(), anyString(), anyString()))
            .thenReturn(List.of(existingCompetition));
        when(repo.save(any(CompetitionEntity.class))).thenReturn(existingCompetition);
        
        // Act
        CompetitionEntity result = competitionService.saveOrUpdate(newCompetition);
        
        // Assert
        verify(repo).save(existingCompetition);
        assertTrue(result.getDays().isEmpty(), "Days should be cleared when URL changes");
        assertEquals(newCompetition.getUrl(), result.getUrl(), "URL should be updated");
    }
    
    @Test
    void shouldNotClearDaysWhenUrlDoesNotChange() {
        // Arrange
        newCompetition.setUrl(existingCompetition.getUrl()); // Same URL
        
        when(repo.findByNameAndBeginDateAndEndDate(anyString(), anyString(), anyString()))
            .thenReturn(List.of(existingCompetition));
        when(repo.save(any(CompetitionEntity.class))).thenReturn(existingCompetition);
        
        // Act
        CompetitionEntity result = competitionService.saveOrUpdate(newCompetition);
        
        // Assert
        verify(repo).save(existingCompetition);
        assertEquals(2, result.getDays().size(), "Days should not be cleared when URL doesn't change");
    }
    
    @Test
    void shouldCreateNewCompetitionWhenNotFound() {
        // Arrange
        when(repo.findByNameAndBeginDateAndEndDate(anyString(), anyString(), anyString()))
            .thenReturn(List.of());
        when(repo.save(any(CompetitionEntity.class))).thenReturn(newCompetition);
        
        // Act
        CompetitionEntity result = competitionService.saveOrUpdate(newCompetition);
        
        // Assert
        verify(repo).save(newCompetition);
        assertEquals(newCompetition, result);
    }
    
    @Test
    void shouldHandleNullUrlsCorrectly() {
        // Arrange - existing competition with null URL
        existingCompetition.setUrl(null);
        newCompetition.setUrl("http://example.com/competition/new"); // New has URL
        
        when(repo.findByNameAndBeginDateAndEndDate(anyString(), anyString(), anyString()))
            .thenReturn(List.of(existingCompetition));
        when(repo.save(any(CompetitionEntity.class))).thenReturn(existingCompetition);
        
        // Act
        CompetitionEntity result = competitionService.saveOrUpdate(newCompetition);
        
        // Assert
        verify(repo).save(existingCompetition);
        assertTrue(result.getDays().isEmpty(), "Days should be cleared when URL changes from null to non-null");
        assertEquals(newCompetition.getUrl(), result.getUrl(), "URL should be updated");
        
        // Test case: new competition has null URL, existing has URL
        reset(repo);
        existingCompetition.setUrl("http://example.com/competition/old");
        existingCompetition.setDays(days); // Restore days
        newCompetition.setUrl(null);
        
        when(repo.findByNameAndBeginDateAndEndDate(anyString(), anyString(), anyString()))
            .thenReturn(List.of(existingCompetition));
        when(repo.save(any(CompetitionEntity.class))).thenReturn(existingCompetition);
        
        // Act again
        result = competitionService.saveOrUpdate(newCompetition);
        
        // Assert again
        verify(repo).save(existingCompetition);
        assertTrue(result.getDays().isEmpty(), "Days should be cleared when URL changes from non-null to null");
        assertNull(result.getUrl(), "URL should be updated to null");
    }
}
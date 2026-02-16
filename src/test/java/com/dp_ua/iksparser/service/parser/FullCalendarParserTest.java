package com.dp_ua.iksparser.service.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.exeption.ExceptionType;
import com.dp_ua.iksparser.exeption.ParsingException;
import com.dp_ua.iksparser.service.Downloader;

public class FullCalendarParserTest {

    @Mock
    private Downloader downloader;

    @InjectMocks
    private FullCalendarParser fullCalendarParser;

    private Document sampleDocument;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        
        // Создаем образец HTML документа для тестирования
        String html = "<html><body>" +
                "<table><tbody>" +
                "<tr><td>Спортивні заходи</td><td>Дата початку</td><td>Дата закінчення</td><td>Назва заходу</td><td>Країна</td><td>Місто</td><td>Організатор</td><td>Представник Федерації</td><td>Результати</td></tr>" +
                "<tr><td>Легка атлетика</td><td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>" +
                "<tr><td>Змагання внесено до календаря</td><td>15.01.2023</td><td>16.01.2023</td><td>Чемпіонат України серед юнаків та дівчат</td><td>Україна</td><td>Київ</td><td>ФЛАУ</td><td>Іванов І.І.</td><td><a href=\"https://example.com/event1\">Результати</a></td></tr>" +
                "<tr><td>Змагання завершено</td><td>20.02.2023</td><td>21.02.2023</td><td>Кубок України</td><td>Україна</td><td>Львів</td><td>ФЛАУ</td><td>Петренко П.П.</td><td><a href=\"https://example.com/event2\">Результати</a></td></tr>" +
                "<tr><td>Змагання внесено до календаря</td><td>10.03.2023</td><td>12.03.2023</td><td>Міжнародний турнір</td><td>Україна</td><td>Одеса</td><td>ФЛАУ</td><td>Сидоренко С.С.</td><td><a href=\"https://example.com/event3\">Результати</a></td></tr>" +
                "<tr><td>Завершення змагань</td><td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>" +
                "</tbody></table>" +
                "</body></html>";
        
        sampleDocument = Jsoup.parse(html);
    }

    @Test
    public void testParseCompetitions_Success() throws ParsingException {
        // Arrange
        when(downloader.getDocument(anyString())).thenReturn(sampleDocument);
        
        // Act
        List<CompetitionEntity> competitions = fullCalendarParser.parseCompetitions(2023);
        
        // Assert
        assertNotNull(competitions);
        assertEquals(3, competitions.size());
        
        // Проверяем первое соревнование
        CompetitionEntity firstCompetition = competitions.get(0);
        assertEquals("Змагання внесено до календаря", firstCompetition.getStatus());
        assertEquals("15.01.2023", firstCompetition.getBeginDate());
        assertEquals("16.01.2023", firstCompetition.getEndDate());
        assertEquals("Чемпіонат України серед юнаків та дівчат", firstCompetition.getName());
        assertEquals("UKR", firstCompetition.getCountry());
        assertEquals("Київ", firstCompetition.getCity());
        assertEquals("https://example.com/event1", firstCompetition.getUrl());
        
        // Проверяем второе соревнование
        CompetitionEntity secondCompetition = competitions.get(1);
        assertEquals("Змагання завершено", secondCompetition.getStatus());
        assertEquals("20.02.2023", secondCompetition.getBeginDate());
        assertEquals("21.02.2023", secondCompetition.getEndDate());
        assertEquals("Кубок України", secondCompetition.getName());
        assertEquals("UKR", secondCompetition.getCountry());
        assertEquals("Львів", secondCompetition.getCity());
        
        // Проверяем, что был вызов метода getDocument с правильным URL
        verify(downloader).getDocument(contains("search_year=2023"));
    }

    @Test(expected = ParsingException.class)
    public void testParseCompetitions_EmptyUrl() throws ParsingException {
        // Arrange
        when(downloader.getDocument(anyString())).thenThrow(new ParsingException("Url is empty", ExceptionType.EMPTY_URL));
        
        // Act & Assert (ожидаем исключение)
        fullCalendarParser.parseCompetitions(2023);
    }

    @Test
    public void testParseCompetitions_NullDocument() throws ParsingException {
        // Arrange
        when(downloader.getDocument(anyString())).thenReturn(null);
        
        // Act
        List<CompetitionEntity> competitions = fullCalendarParser.parseCompetitions(2023);
        
        // Assert
        assertNotNull(competitions);
        assertTrue(competitions.isEmpty());
    }

    @Test
    public void testGetURL() throws Exception {
        // Используем рефлексию для доступа к приватному методу
        java.lang.reflect.Method method = FullCalendarParser.class.getDeclaredMethod("getURL", int.class);
        method.setAccessible(true);
        
        // Act
        String url = (String) method.invoke(fullCalendarParser, 2023);
        
        // Assert
        assertTrue(url.contains("search_year=2023"));
        assertTrue(url.contains("iks.org.ua/calendar"));
    }
}
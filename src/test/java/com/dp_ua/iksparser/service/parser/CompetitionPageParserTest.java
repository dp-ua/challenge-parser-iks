package com.dp_ua.iksparser.service.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dp_ua.iksparser.dba.entity.DayEntity;
import com.dp_ua.iksparser.dba.entity.EventEntity;
import com.dp_ua.iksparser.exeption.ParsingException;

@ExtendWith(MockitoExtension.class)
class CompetitionPageParserTest {

    @Mock
    private ServiceParser serviceParser;

    @InjectMocks
    private CompetitionPageParser competitionPageParser;

    private Document sampleDocument;

    @BeforeEach
    void setUp() {
        // Создаем образец HTML документа для тестирования
        String html = "<html><body>" +
                "<ul id='timetable'>" +
                "<li><a href='#timetable-151223'>15.12.23 (День 1 | Day 1)</a></li>" +
                "<li><a href='#timetable-161223'>16.12.23 (День 2 | Day 2)</a></li>" +
                "</ul>" +
                "<div id='timetable-151223'>" +
                "<table><tbody>" +
                "<tr>" +
                "<td>10:00</td>" +
                "<td><span>100 м. (Чоловіки)</span></td>" +
                "<td>Дорослі</td>" +
                "<td>Забіг 1</td>" +
                "<td><button onclick=\"location.href='https://example.com/start'\">Стартовий</button></td>" +
                "<td><button onclick=\"location.href='https://example.com/result'\">Результати</button></td>" +
                "</tr>" +
                "<tr>" +
                "<td>11:00</td>" +
                "<td><span>Естафетний 4x100 м.</span></td>" +
                "<td>Дорослі</td>" +
                "<td>Фінал</td>" +
                "<td><button onclick=\"location.href='https://example.com/start2'\">Стартовий</button></td>" +
                "</tr>" +
                "<tr>" +
                "<td>12:00</td>" +
                "<td><span>Стрибки у довжину</span></td>" +
                "<td>Юніори</td>" +
                "<td>Фінал</td>" +
                "<td><button onclick=\"location.href='https://example.com/start3'\">Стартовий</button></td>" +
                "<td><button onclick=\"location.href='https://example.com/result3'\">Результати</button></td>" +
                "</tr>" +
                "</tbody></table>" +
                "</div>" +
                "<div id='timetable-161223'>" +
                "<table><tbody>" +
                "<tr>" +
                "<td>09:30</td>" +
                "<td><span>200 м. (Жінки)</span></td>" +
                "<td>Дорослі</td>" +
                "<td>Забіг 1</td>" +
                "<td><button onclick=\"location.href='https://example.com/start4'\">Стартовий</button></td>" +
                "</tr>" +
                "</tbody></table>" +
                "</div>" +
                "</body></html>";

        sampleDocument = Jsoup.parse(html);

        // Настраиваем мок для ServiceParser
        lenient().when(serviceParser.parseDay(anyString())).thenAnswer(invocation -> {
            String input = invocation.getArgument(0);
            if (input.contains("151223")) {
                return new DayEntity("15.12.23", "151223", "День 1", "Day 1");
            } else {
                return new DayEntity("16.12.23", "161223", "День 2", "Day 2");
            }
        });
    }

    @Test
    void testGetUnsavedEvents_Success() {
        // Arrange
        DayEntity day = new DayEntity("15.12.23", "151223", "День 1", "Day 1");

        // Act
        List<EventEntity> events = competitionPageParser.getUnsavedEvents(sampleDocument, day);

        // Assert
        assertNotNull(events);
        assertEquals(2, events.size()); // Должно быть 2, так как один event - эстафетный и фильтруется

        // Проверяем первое событие
        EventEntity firstEvent = events.get(0);
        assertEquals("10:00", firstEvent.getTime());
        assertEquals("100 м. (Чоловіки)", firstEvent.getEventName());
        assertEquals("Дорослі", firstEvent.getCategory());
        assertEquals("забіг", firstEvent.getRound());
        assertEquals("https://example.com/start", firstEvent.getStartListUrl());
        assertEquals("https://example.com/result", firstEvent.getResultUrl());

        // Проверяем второе событие
        EventEntity secondEvent = events.get(1);
        assertEquals("12:00", secondEvent.getTime());
        assertEquals("Стрибки у довжину", secondEvent.getEventName());
        assertEquals("Юніори", secondEvent.getCategory());
        assertEquals("фінал", secondEvent.getRound());
    }

    @Test
    void testGetUnsavedUnfilledDays_Success() throws ParsingException {
        // Act
        List<DayEntity> days = competitionPageParser.getUnsavedUnfilledDays(sampleDocument);

        // Assert
        assertNotNull(days);
        assertEquals(2, days.size());

        // Проверяем первый день
        DayEntity firstDay = days.get(0);
        assertEquals("15.12.23", firstDay.getDate());
        assertEquals("151223", firstDay.getDateId());
        assertEquals("День 1", firstDay.getDayName());
        assertEquals("Day 1", firstDay.getDayNameEn());

        // Проверяем второй день
        DayEntity secondDay = days.get(1);
        assertEquals("16.12.23", secondDay.getDate());
        assertEquals("161223", secondDay.getDateId());
        assertEquals("День 2", secondDay.getDayName());
        assertEquals("Day 2", secondDay.getDayNameEn());
    }

    @Test
    void testGetUnsavedUnfilledDays_EmptyDays() {
        // Arrange
        Document emptyDocument = Jsoup.parse("<html><body></body></html>");

        // Act & Assert (ожидаем исключение)
        assertThrows(ParsingException.class, () -> competitionPageParser.getUnsavedUnfilledDays(emptyDocument));
    }

    @Test
    void testGetUnsavedEvents_EmptyEvents() {
        // Arrange
        Document emptyDocument = Jsoup.parse("<html><body><div id='timetable-151223'><table><tbody></tbody></table></div></body></html>");
        DayEntity day = new DayEntity("15.12.23", "151223", "День 1", "Day 1");

        // Act & Assert (ожидаем исключение)
        assertThrows(IllegalStateException.class, () -> competitionPageParser.getUnsavedEvents(emptyDocument, day));
    }

    @Test
    void testParseEvent_CompleteData() {
        // Arrange
        Element row = sampleDocument.select("div#timetable-151223 table tbody tr").first();

        // Act
        EventEntity event = competitionPageParser.parseEvent(row);

        // Assert
        assertEquals("10:00", event.getTime());
        assertEquals("100 м. (Чоловіки)", event.getEventName());
        assertEquals("Дорослі", event.getCategory());
        assertEquals("забіг", event.getRound());
        assertEquals("https://example.com/start", event.getStartListUrl());
        assertEquals("https://example.com/result", event.getResultUrl());
    }

    @Test
    void testParseEvent_MissingResultUrl() {
        // Arrange - используем второй ряд, где нет кнопки результатов
        Element row = sampleDocument.select("div#timetable-151223 table tbody tr").get(1);

        // Act
        EventEntity event = competitionPageParser.parseEvent(row);

        // Assert
        assertEquals("11:00", event.getTime());
        assertEquals("Естафетний 4x100 м.", event.getEventName());
        assertEquals("Дорослі", event.getCategory());
        assertEquals("фінал", event.getRound());
        assertEquals("https://example.com/start2", event.getStartListUrl());
        assertEquals("", event.getResultUrl()); // Нет URL результатов
    }

    @Test
    void testParseEvent_MalformedHTML() {
        // Arrange - создаем элемент с неполными данными
        Document malformedDoc = Jsoup.parse("<html><body><table><tbody><tr><td>09:00</td></tr></tbody></table></body></html>");
        Element malformedRow = malformedDoc.select("tr").first();

        // Act
        EventEntity event = competitionPageParser.parseEvent(malformedRow);

        // Assert - проверяем что парсер не упадет при некорректных данных
        assertEquals("09:00", event.getTime());
        assertEquals("", event.getEventName());
        assertEquals("", event.getCategory());
        assertEquals("", event.getRound());
        assertEquals("", event.getStartListUrl());
        assertEquals("", event.getResultUrl());
    }

}

package com.dp_ua.iksparser.service.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dp_ua.iksparser.dba.entity.CoachEntity;
import com.dp_ua.iksparser.dba.entity.HeatEntity;
import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import com.dp_ua.iksparser.dba.service.CoachService;
import com.dp_ua.iksparser.dba.service.HeatLineService;
import com.dp_ua.iksparser.dba.service.HeatService;
import com.dp_ua.iksparser.dba.service.ParticipantService;

public class EventPageParserTest {

    @Mock
    private HeatService heatService;

    @Mock
    private ParticipantService participantService;

    @Mock
    private CoachService coachService;

    @Mock
    private HeatLineService heatLineService;

    @Mock
    private ServiceParser serviceParser;

    @InjectMocks
    private EventPageParser eventPageParser;

    private Document sampleDocument;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        // Создаем образец HTML документа для тестирования
        String html = "<html><body>" +
                "<div class='table-responsive'>" +
                "<table class='table'>" +
                "<tr>" +
                "<td>1</td>" +
                "<td>123</td>" +
                "<td></td>" +
                "<td></td>" +
                "<td><a href='https://example.com/participant/1'>Профиль</a></td>" +
                "<td>Петренко</td>" +
                "<td>Іван</td>" +
                "<td>2000</td>" +
                "<td>Київ</td>" +
                "<td>ДЮСШ №1</td>" +
                "<td>Сидоренко В.А., Іваненко А.Б.</td>" +
                "</tr>" +
                "<tr>" +
                "<td>2</td>" +
                "<td>456</td>" +
                "<td></td>" +
                "<td></td>" +
                "<td><a href='https://example.com/participant/2'>Профиль</a></td>" +
                "<td>Коваленко</td>" +
                "<td>Олег</td>" +
                "<td>2001</td>" +
                "<td>Львів</td>" +
                "<td>СДЮШОР</td>" +
                "<td>Петров С.С.</td>" +
                "</tr>" +
                "</table>" +
                "</div>" +
                "<div class='table-responsive'>" +
                "<table class='table'>" +
                "<tr>" +
                "<td>1</td>" +
                "<td>789</td>" +
                "<td></td>" +
                "<td></td>" +
                "<td><a href='https://example.com/participant/3'>Профиль</a></td>" +
                "<td>Шевченко</td>" +
                "<td>Микола</td>" +
                "<td>1999</td>" +
                "<td>Одеса</td>" +
                "<td>СК Олімп</td>" +
                "<td>Левченко О.В.</td>" +
                "</tr>" +
                "</table>" +
                "</div>" +
                "</body></html>";

        sampleDocument = Jsoup.parse(html);

        // Настройки моков
        when(serviceParser.cleanTextFromEmoji(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(participantService.findParticipant(anyString(), anyString(), anyString())).thenReturn(null);
        when(coachService.findByName(anyString())).thenReturn(null);
    }

    @Test
    public void testGetHeats_Success() {
        // Act
        List<HeatEntity> heats = eventPageParser.getHeats(sampleDocument);

        // Assert
        assertNotNull(heats);
        assertEquals(2, heats.size());

        // Проверяем количество сохранений
        verify(heatService, times(2)).save(any(HeatEntity.class));
        verify(participantService, times(3)).save(any(ParticipantEntity.class));
        verify(heatLineService, times(3)).save(any(HeatLineEntity.class));

        // Проверяем создание тренеров (4 тренера в документе)
        verify(coachService, times(4)).save(any(CoachEntity.class));
    }

    @Test
    public void testGetHeats_ExistingParticipant() {
        // Arrange
        ParticipantEntity existingParticipant = new ParticipantEntity();
        existingParticipant.setSurname("Петренко");
        existingParticipant.setName("Іван");
        existingParticipant.setBorn("2000");

        when(participantService.findParticipant("Петренко", "Іван", "2000")).thenReturn(existingParticipant);

        // Act
        List<HeatEntity> heats = eventPageParser.getHeats(sampleDocument);

        // Assert
        verify(participantService).save(existingParticipant); // Обновить URL

        // Проверяем, что было создано только 2 новых участника (всего 3 в документе, но один уже существует)
        verify(participantService, times(3)).save(any(ParticipantEntity.class));
    }

    @Test
    public void testGetHeats_ExistingCoach() {
        // Arrange
        CoachEntity existingCoach = new CoachEntity();
        existingCoach.setName("Сидоренко В.А.");

        when(coachService.findByName("Сидоренко В.А.")).thenReturn(existingCoach);

        // Act
        List<HeatEntity> heats = eventPageParser.getHeats(sampleDocument);

        // Assert
        // Проверяем, что было создано только 3 новых тренера (всего 4 в документе, но один уже существует)
        verify(coachService, times(3)).save(any(CoachEntity.class));

        // Проверяем, что существующий тренер был добавлен к heatLine
        verify(coachService).save(existingCoach);
    }

    @Test
    public void testGetHeats_EmptyHeat() {
        // Arrange
        Document documentWithEmptyHeat = Jsoup.parse("<html><body>" +
                "<div class='table-responsive'>" +
                "<table class='table'>" +
                "<tr><td colspan='11'>Нет участников</td></tr>" +
                "</table>" +
                "</div>" +
                "</body></html>");

        // Act
        List<HeatEntity> heats = eventPageParser.getHeats(documentWithEmptyHeat);

        // Assert
        assertEquals(0, heats.size()); // Пустые забеги не добавляются
        verify(heatService).delete(any(HeatEntity.class)); // Убеждаемся, что пустой забег удаляется
    }

    @Test
    public void testGetHeats_UpdateParticipantUrl() {
        // Arrange
        ParticipantEntity participantWithoutUrl = new ParticipantEntity();
        participantWithoutUrl.setSurname("Петренко");
        participantWithoutUrl.setName("Іван");
        participantWithoutUrl.setBorn("2000");
        participantWithoutUrl.setUrl(""); // Пустой URL

        when(participantService.findParticipant("Петренко", "Іван", "2000")).thenReturn(participantWithoutUrl);

        // Act
        eventPageParser.getHeats(sampleDocument);

        // Assert
        assertEquals("https://example.com/participant/1", participantWithoutUrl.getUrl());
        verify(participantService).save(participantWithoutUrl);
    }

    @Test
    public void testGetHeats_ParticipantWithExistingUrl() {
        // Arrange
        ParticipantEntity participantWithUrl = new ParticipantEntity();
        participantWithUrl.setSurname("Петренко");
        participantWithUrl.setName("Іван");
        participantWithUrl.setBorn("2000");
        participantWithUrl.setUrl("https://example.com/existing");

        when(participantService.findParticipant("Петренко", "Іван", "2000")).thenReturn(participantWithUrl);

        // Act
        eventPageParser.getHeats(sampleDocument);

        // Assert - URL не должен обновиться
        assertEquals("https://example.com/existing", participantWithUrl.getUrl());
    }

    @Test
    public void testGetHeats_CleanTextFromEmoji() {
        // Arrange
        when(serviceParser.cleanTextFromEmoji("Петренко🏆")).thenReturn("Петренко");

        Document docWithEmoji = Jsoup.parse("<html><body>" +
                "<div class='table-responsive'>" +
                "<table class='table'>" +
                "<tr>" +
                "<td>1</td><td>123</td><td></td><td></td>" +
                "<td><a href='https://example.com/participant/1'>Профиль</a></td>" +
                "<td>Петренко🏆</td>" +
                "<td>Іван</td>" +
                "<td>2000</td>" +
                "<td>Київ</td>" +
                "<td>ДЮСШ №1</td>" +
                "<td>Тренер</td>" +
                "</tr>" +
                "</table>" +
                "</div>" +
                "</body></html>");

        // Act
        eventPageParser.getHeats(docWithEmoji);

        // Assert
        verify(serviceParser).cleanTextFromEmoji("Петренко🏆");
    }

}

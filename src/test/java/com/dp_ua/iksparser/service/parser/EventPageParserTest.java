package com.dp_ua.iksparser.service.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dp_ua.iksparser.dba.entity.CoachEntity;
import com.dp_ua.iksparser.dba.entity.HeatEntity;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import com.dp_ua.iksparser.dba.service.CoachService;
import com.dp_ua.iksparser.dba.service.HeatLineService;
import com.dp_ua.iksparser.dba.service.HeatService;
import com.dp_ua.iksparser.dba.service.ParticipantService;

@ExtendWith(MockitoExtension.class)
class EventPageParserTest {

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

    @BeforeEach
    void setUp() {
        // Создаем образец HTML документа для тестирования
        // Структура ячеек: 0-lane, 1-bib, 2-3-пустые, 4-url, 5-surname, 6-name, 7-born, 8-region, 9-team, 10-coaches
        String html = "<html><body>" +
                "<div class='table-responsive'>" +
                "<table class='table'>" +
                "<tr>" +
                "<td>1</td>" +
                "<td>123</td>" +
                "<td></td>" +
                "<td></td>" +
                "<td><a href='https://example.com/participant/1'>Профиль</a></td>" +
                "<td><span>Петренко</span></td>" +  // Добавили <span> для вложенности
                "<td><span>Іван</span></td>" +      // Добавили <span> для вложенности
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
                "<td><span>Коваленко</span></td>" +
                "<td><span>Олег</span></td>" +
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
                "<td><span>Шевченко</span></td>" +
                "<td><span>Микола</span></td>" +
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
        lenient().when(serviceParser.cleanTextFromEmoji(anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(participantService.findParticipant(anyString(), anyString(), anyString()))
                .thenReturn(null);
        lenient().when(coachService.findByName(anyString()))
                .thenReturn(null);
    }

    @Test
    void testGetHeats_Success() {
        // Act
        List<HeatEntity> heats = eventPageParser.getHeats(sampleDocument);

        // Assert
        assertNotNull(heats);
        assertEquals(2, heats.size());

        // Проверяем количество сохранений
        // 2 heat (по одному на каждую таблицу)
        verify(heatService, times(2)).save(any(HeatEntity.class));

        // 3 участника × 2 сохранения (createNewParticipant + saveRelationBetweenHeatLineAndParticipant)
        verify(participantService, times(6)).save(any(ParticipantEntity.class));

        // 3 heatLine × 1 (getHeatLineFromRow) + 3 × 1 (saveRelationBetweenHeatLineAndParticipant) + 4 тренера × 1
        // (saveRelationsBetweenCoachAndHeatLine)
        verify(heatLineService, times(10)).save(any(com.dp_ua.iksparser.dba.entity.HeatLineEntity.class));

        // 4 тренера × 2 сохранения (createNewCoach + saveRelationsBetweenCoachAndHeatLine)
        verify(coachService, times(8)).save(any(CoachEntity.class));
    }

    @Test
    void testGetHeats_ExistingParticipant() {
        // Arrange
        ParticipantEntity existingParticipant = new ParticipantEntity();
        existingParticipant.setSurname("Петренко");
        existingParticipant.setName("Іван");
        existingParticipant.setBorn("2000");
        existingParticipant.setUrl("https://example.com/existing");

        when(participantService.findParticipant("Петренко", "Іван", "2000")).thenReturn(existingParticipant);

        // Act
        List<HeatEntity> heats = eventPageParser.getHeats(sampleDocument);

        // Assert
        assertNotNull(heats);
        assertEquals(2, heats.size());

        // Существующий участник: 1 раз (saveRelationBetweenHeatLineAndParticipant, без updateParticipantUrl т.к. URL уже есть)
        // Новые участники: 2 × 2 = 4
        // Итого: 5
        verify(participantService, times(5)).save(any(ParticipantEntity.class));
    }

    @Test
    void testGetHeats_ExistingCoach() {
        // Arrange
        CoachEntity existingCoach = new CoachEntity();
        existingCoach.setName("Сидоренко В.А.");

        when(coachService.findByName("Сидоренко В.А.")).thenReturn(existingCoach);

        // Act
        List<HeatEntity> heats = eventPageParser.getHeats(sampleDocument);

        // Assert
        assertNotNull(heats);
        assertEquals(2, heats.size());

        // Существующий тренер "Сидоренко В.А.": 1 раз (saveRelationsBetweenCoachAndHeatLine)
        // Новые тренеры (Іваненко А.Б., Петров С.С., Левченко О.В.): 3 × 2 = 6
        // Итого: 7
        verify(coachService, times(7)).save(any(CoachEntity.class));
    }

    @Test
    void testGetHeats_EmptyHeat() {
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
        verify(heatService, times(1)).save(any(HeatEntity.class)); // Heat создается
        verify(heatService, times(1)).delete(any(HeatEntity.class)); // Но потом удаляется
    }

    @Test
    void testGetHeats_UpdateParticipantUrl() {
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

        // Существующий участник с пустым URL: 2 раза (updateParticipantUrl + saveRelationBetweenHeatLineAndParticipant)
        // Новые участники: 2 × 2 = 4
        // Итого: 6
        verify(participantService, times(6)).save(any(ParticipantEntity.class));
    }

    @Test
    void testGetHeats_ParticipantWithExistingUrl() {
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

        // Существующий участник с URL: 1 раз (saveRelationBetweenHeatLineAndParticipant, без updateParticipantUrl)
        // Новые участники: 2 × 2 = 4
        // Итого: 5
        verify(participantService, times(5)).save(any(ParticipantEntity.class));
    }

    @Test
    void testGetHeats_CleanTextFromEmoji() {
        // Arrange
        when(serviceParser.cleanTextFromEmoji("Петренко🏆")).thenReturn("Петренко");

        Document docWithEmoji = Jsoup.parse("<html><body>" +
                "<div class='table-responsive'>" +
                "<table class='table'>" +
                "<tr>" +
                "<td>1</td><td>123</td><td></td><td></td>" +
                "<td><a href='https://example.com/participant/1'>Профиль</a></td>" +
                "<td><span>Петренко🏆</span></td>" +  // Добавили <span>
                "<td><span>Іван</span></td>" +        // Добавили <span>
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
        verify(serviceParser, times(1)).cleanTextFromEmoji("Петренко🏆");
        verify(serviceParser, times(1)).cleanTextFromEmoji("Іван");
    }

    @Test
    void testGetHeats_MultipleCoachesPerParticipant() {
        // Arrange
        Document docWithMultipleCoaches = Jsoup.parse("<html><body>" +
                "<div class='table-responsive'>" +
                "<table class='table'>" +
                "<tr>" +
                "<td>1</td><td>123</td><td></td><td></td>" +
                "<td><a href='https://example.com/participant/1'>Профиль</a></td>" +
                "<td><span>Петренко</span></td>" +
                "<td><span>Іван</span></td>" +
                "<td>2000</td>" +
                "<td>Київ</td>" +
                "<td>ДЮСШ №1</td>" +
                "<td>Тренер1, Тренер2, Тренер3</td>" +  // 3 тренера
                "</tr>" +
                "</table>" +
                "</div>" +
                "</body></html>");

        // Act
        List<HeatEntity> heats = eventPageParser.getHeats(docWithMultipleCoaches);

        // Assert
        assertNotNull(heats);
        assertEquals(1, heats.size());

        // 3 тренера × 2 сохранения = 6
        verify(coachService, times(6)).save(any(CoachEntity.class));
    }

    @Test
    void testGetHeats_NoCoaches() {
        // Arrange
        Document docWithoutCoaches = Jsoup.parse("<html><body>" +
                "<div class='table-responsive'>" +
                "<table class='table'>" +
                "<tr>" +
                "<td>1</td><td>123</td><td></td><td></td>" +
                "<td><a href='https://example.com/participant/1'>Профиль</a></td>" +
                "<td><span>Петренко</span></td>" +
                "<td><span>Іван</span></td>" +
                "<td>2000</td>" +
                "<td>Київ</td>" +
                "<td>ДЮСШ №1</td>" +
                "<td></td>" +  // Нет тренеров
                "</tr>" +
                "</table>" +
                "</div>" +
                "</body></html>");

        // Act
        List<HeatEntity> heats = eventPageParser.getHeats(docWithoutCoaches);

        // Assert
        assertNotNull(heats);
        assertEquals(1, heats.size());

        // Тренеры не создаются
        verify(coachService, times(0)).save(any(CoachEntity.class));
    }

}

package com.dp_ua.iksparser.bot.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.dp_ua.iksparser.App;
import com.dp_ua.iksparser.bot.Bot;
import com.dp_ua.iksparser.exeption.NotForMeException;

import lombok.SneakyThrows;

@SpringBootTest
class TextCommandDetectorImplTest {

    @MockBean
    CommandProvider commandProvider;
    @MockBean
    App app;
    @Autowired
    TextCommandDetectorImpl textCmdDetector;
    @MockBean
    Bot bot;

    @BeforeEach
    void additionalSetUp() {
        when(commandProvider.getCommands()).thenReturn(new ArrayList<>() {{
            add(new TestCommand());
        }});
        when(bot.getBotUsername()).thenReturn("testBot");
    }

    @ParameterizedTest
    @ValueSource(strings = {"/test", "/test@testBot", "/TEST", "/test some text"})
    @SneakyThrows
    void shouldParseCommand_VariousCases(String text) {
        String expected = "[TestCommand]:{test},alt{[]}";
        List<CommandInterface> result = textCmdDetector.getParsedCommands(text);
        String actual = getJoin(result);
        assertEquals(expected, actual);
    }

    @Test
    @SneakyThrows
    void shouldNotParseCommand_Case1() {
        String text = "/tweets";
        String actual = textCmdDetector.getParsedCommands(text).toString();
        String expected = "[]";
        assertEquals(expected, actual);
    }

    @Test
    void shouldGetException_whenWrongBotName() {
        String text = "/test@otherBot";
        assertThrows(NotForMeException.class, () -> textCmdDetector.getParsedCommands(text));
    }

    private String getJoin(List<CommandInterface> list) {
        return list
                .stream()
                .map(CommandInterface::logString)
                .collect(Collectors.joining
                        (";"));
    }

}

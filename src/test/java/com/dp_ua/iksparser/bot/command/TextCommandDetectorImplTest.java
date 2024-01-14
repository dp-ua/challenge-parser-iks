package com.dp_ua.iksparser.bot.command;

import com.dp_ua.iksparser.MockBotControllerTest;
import com.dp_ua.iksparser.bot.Bot;
import com.dp_ua.iksparser.exeption.NotForMeException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.when;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TextCommandDetectorImplTest extends MockBotControllerTest {
    @MockBean
    CommandProvider commandProvider;

    @Autowired
    TextCommandDetectorImpl textCmdDetector;
    @MockBean
    Bot bot;

    @Override
    public void additionalSetUp() {
        when(commandProvider.getCommands()).thenReturn(new ArrayList<>() {{
            add(new TestCommand());
        }});
        when(bot.getBotUsername()).thenReturn("testBot");
    }

    @Test
    public void shouldParseCommand_Case1() throws NotForMeException {
        String text = "/test";
        String expected = "[TestCommand]:{test},alt{[]}";

        List<CommandInterface> result = textCmdDetector.getParsedCommands(text);
        String actual = getJoin(result);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldParseCommand_Case2() throws NotForMeException {
        String text = "/test@testBot";
        String expected = "[TestCommand]:{test},alt{[]}";

        List<CommandInterface> result = textCmdDetector.getParsedCommands(text);
        String actual = getJoin(result);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldParseCommand_CapitalLetters() throws NotForMeException {
        String text = "/TEST";
        String expected = "[TestCommand]:{test},alt{[]}";

        List<CommandInterface> result = textCmdDetector.getParsedCommands(text);
        String actual = getJoin(result);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldParseCommand_Case4() throws NotForMeException {
        String text = "/test some text";
        String expected = "[TestCommand]:{test},alt{[]}";

        List<CommandInterface> result = textCmdDetector.getParsedCommands(text);
        String actual = getJoin(result);

        Assert.assertEquals(expected, actual);
    }


    @Test
    public void shouldNotParseCommand_Case1() throws NotForMeException {
        String text = "/tweets";
        String actual = textCmdDetector.getParsedCommands(text).toString();
        String expected = "[]";
        Assert.assertEquals(expected, actual);
    }

    @Test(expected = NotForMeException.class)
    public void shouldGetException_whenWrongBotName() throws NotForMeException {
        // given
        String text = "/test@otherBot";

        // when call method
        textCmdDetector.getParsedCommands(text);

        // then never happens
        Assert.fail();
    }

    private String getJoin(List<CommandInterface> list) {
        return list
                .stream()
                .map(CommandInterface::logString)
                .collect(Collectors.joining(";"));
    }
}
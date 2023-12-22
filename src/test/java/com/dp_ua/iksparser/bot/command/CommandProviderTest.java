package com.dp_ua.iksparser.bot.command;

import com.dp_ua.iksparser.MockBotControllerTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CommandProviderTest extends MockBotControllerTest {
    @Autowired
    CommandProvider commandProvider;

    @Test
    public void checkAllCommands() {
        List<String> allCommands = new ArrayList<>();
        commandProvider.getCommands().forEach(command -> {
            allCommands.addAll(command.allSimpleCommands());
            allCommands.addAll(command.fullStringCommands());
            allCommands.addAll(command.partOfStringCommands());
        });

        String actual = String.join("\n", allCommands);
        Assert.assertEquals(
                "help\n" +
                        "start",
                actual);
    }
}
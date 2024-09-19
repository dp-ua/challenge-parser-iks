package com.dp_ua.iksparser.bot.command;

import com.dp_ua.iksparser.App;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CommandProviderTest {
    @MockBean
    App app;
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
                """
                        duplicates
                        send
                        update
                        deletemessage
                        help
                        menu
                        searchbybibnumber
                        searchbybibnumberwithbib
                        searchbycoach
                        searchbycoachwithname
                        searchbyname
                        searchbynamewithname
                        start
                        competition
                        competitionnotloaded
                        competitions
                        parcompdet
                        shpartdet
                        participants
                        salps
                        showhtlincomfpart
                        subscribe
                        subscriptions
                        subscriptionslist
                        unsubscribe""",
                actual);
    }

    @Test
    public void shouldReturnMenuCommands() {
        List<BotCommand> commands = commandProvider.menuCommands();
        String result = commands.stream()
                .map(command -> "/" + command.getCommand() + " " + command.getDescription())
                .reduce("", (s1, s2) -> s1 + "\n" + s2)
                .substring(1);

        Assert.assertEquals("""
                        /menu головне меню
                        /start Розпочати роботу
                        /competitions Список змагань"""
                , result);
    }
}
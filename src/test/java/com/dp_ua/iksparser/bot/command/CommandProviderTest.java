package com.dp_ua.iksparser.bot.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import com.dp_ua.iksparser.App;

@SpringBootTest
class CommandProviderTest {

    @MockBean
    App app;
    @Autowired
    CommandProvider commandProvider;

    @Test
    void checkAllCommands() {
        List<String> allCommands = new ArrayList<>();
        commandProvider.getCommands().forEach(command -> {
            allCommands.addAll(command.allSimpleCommands());
            allCommands.addAll(command.fullStringCommands());
            allCommands.addAll(command.partOfStringCommands());
        });

        String actual = String.join("\n", allCommands);
        assertEquals(
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
                        shpartdet
                        participants
                        salps
                        shlcfp
                        subscriptions
                        subscriptionslist""",
                actual);
    }

    @Test
    void shouldReturnMenuCommands() {
        List<BotCommand> commands = commandProvider.menuCommands();
        String result = commands.stream()
                .map(command -> "/" + command.getCommand() + " " + command.getDescription())
                .reduce("", (s1, s2) -> s1 + "\n" + s2)
                .substring(1);

        assertEquals("""
                        /menu Меню
                        /start Розпочати роботу
                        /competitions 🏆 Список змагань
                        /participants 🏃Список атлетів
                        /subscriptions ⭐ Підписки"""
                , result);
    }

}

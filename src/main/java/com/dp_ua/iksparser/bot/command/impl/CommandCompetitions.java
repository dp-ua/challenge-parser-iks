package com.dp_ua.iksparser.bot.command.impl;

import com.dp_ua.iksparser.bot.abilities.CompetitionFacade;
import com.dp_ua.iksparser.bot.command.CommandInterface;
import com.dp_ua.iksparser.bot.message.Message;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@ToString
@EqualsAndHashCode
public class CommandCompetitions implements CommandInterface {
    private final String command = "competitions";
    private final boolean isInTextCommand = false;
    @Autowired
    CompetitionFacade competitionFacade;

    @Override
    public String command() {
        return command;
    }

    @Override
    public void execute(Message message) {
        String chatId = message.getChatId();
        competitionFacade.showCompetitions(chatId);
    }
}

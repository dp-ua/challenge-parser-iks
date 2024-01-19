package com.dp_ua.iksparser.bot.command.impl;

import com.dp_ua.iksparser.bot.Icon;
import com.dp_ua.iksparser.bot.abilities.CompetitionFacade;
import com.dp_ua.iksparser.bot.command.BaseCommand;
import com.dp_ua.iksparser.bot.message.Message;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@ToString
public class CommandCompetition extends BaseCommand {
    private final String command = "competition";
    private final boolean isInTextCommand = false;
    @Autowired
    CompetitionFacade competitionFacade;

    @Override
    public String command() {
        return command;
    }

    @Override
    protected String getTextForCallBackAnswer(Message message) {
        return Icon.INFO + " Дивимось інформацію по змаганню " + Icon.INFO;
    }

    @Override
    protected void perform(Message message) {
        String chatId = message.getChatId();
        int commandArgument = getCommandArgument(message.getMessageText());
        competitionFacade.showCompetition(chatId, commandArgument, message.getEditMessageId());
    }
}
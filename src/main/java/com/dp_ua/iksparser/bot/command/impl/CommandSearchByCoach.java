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
public class CommandSearchByCoach extends BaseCommand {
    public static final String command = "searchbycoach";
    private final boolean isInTextCommand = false;
    @Autowired
    private CompetitionFacade competitionFacade;

    @Override
    public String command() {
        return command;
    }

    @Override
    protected String getTextForCallBackAnswer(Message message) {
        return Icon.INFO + " Будемо шукати дані по тренеру " + Icon.INFO;
    }

    @Override
    protected void perform(Message message) {
        String chatId = message.getChatId();
        int commandArgument = getCommandArgument(message.getMessageText());
        competitionFacade.startSearchByCoach(chatId, commandArgument, message.getEditMessageId());
    }
}

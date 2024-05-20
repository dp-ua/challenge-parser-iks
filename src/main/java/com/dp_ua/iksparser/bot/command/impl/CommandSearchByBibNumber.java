package com.dp_ua.iksparser.bot.command.impl;

import com.dp_ua.iksparser.bot.Icon;
import com.dp_ua.iksparser.bot.abilities.competition.CompetitionFacade;
import com.dp_ua.iksparser.bot.command.BaseCommand;
import com.dp_ua.iksparser.bot.message.Message;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ToString
public class CommandSearchByBibNumber extends BaseCommand {
    public static final String command = "searchbybibnumber";
    private final boolean isInTextCommand = false;
    @Autowired
    private CompetitionFacade competitionFacade;

    @Override
    public String command() {
        return command;
    }

    @Override
    protected String getTextForCallBackAnswer(Message message) {
        return Icon.INFO + " Шукаємо атлета по номеру " + Icon.INFO;
    }

    @Override
    protected void perform(Message message) {
        String chatId = message.getChatId();
        long commandArgument = getCommandArgument(message.getMessageText());
        competitionFacade.startSearchByBibNumber(chatId, commandArgument, message.getEditMessageId());
    }
}

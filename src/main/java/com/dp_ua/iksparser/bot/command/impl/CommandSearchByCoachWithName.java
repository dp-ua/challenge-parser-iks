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
public class CommandSearchByCoachWithName extends BaseCommand {
    private final static String command = "searchbycoachwithname";
    private final boolean isInTextCommand = false;
    @Autowired
    CompetitionFacade competitionFacade;

    @Override
    public String command() {
        return command;
    }

    @Override
    protected String getTextForCallBackAnswer(Message message) {
        return Icon.INFO + " Шукаємо дані по по тренеру " + Icon.INFO;
    }

    @Override
    protected void perform(Message message) {
        String chatId = message.getChatId();
        String commandArgument = getCommandArgumentString(message.getMessageText());
        competitionFacade.searchingByCoachWithName(chatId, commandArgument, message.getEditMessageId());
    }

    public static String getTextForState(String competitionId) {
        return "/" + command + " {\"id\":\"" + competitionId + "\",\"coachName\":\"{}\"}";
    }
}

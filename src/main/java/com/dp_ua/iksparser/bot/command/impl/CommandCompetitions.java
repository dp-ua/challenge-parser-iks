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
public class CommandCompetitions extends BaseCommand {
    private final String command = "competitions";
    private final boolean isInTextCommand = false;
    @Autowired
    CompetitionFacade competitionFacade;

    @Override
    public String command() {
        return command;
    }

    @Override
    protected String getTextForCallBackAnswer(Message message) {
        return Icon.PREVIOUS + " дивитись змагання " + Icon.NEXT;
    }

    @Override
    protected void perform(Message message) {
        String chatId = message.getChatId();
        int commandArgument = getCommandArgument(message.getMessageText());
        if (message.hasCallbackQuery())
            competitionFacade.showCompetitions(chatId, commandArgument, message.getEditMessageId());
    }

    private int getCommandArgument(String text) {
        if (text.startsWith("/" + command)) {
            String argument = text.substring(command.length() + 1).trim();
            return argument.isEmpty() ? 0 : Integer.parseInt(argument);
        }
        return 0;
    }
}

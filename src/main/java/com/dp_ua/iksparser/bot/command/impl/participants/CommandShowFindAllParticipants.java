package com.dp_ua.iksparser.bot.command.impl.participants;

import com.dp_ua.iksparser.bot.Icon;
import com.dp_ua.iksparser.bot.abilities.participant.ParticipantFacade;
import com.dp_ua.iksparser.bot.command.BaseCommand;
import com.dp_ua.iksparser.bot.message.Message;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.dp_ua.iksparser.bot.command.CommandArgumentName.PAGE;
import static com.dp_ua.iksparser.bot.command.CommandArgumentName.SEARCH;


@Component
@ToString
public class CommandShowFindAllParticipants extends BaseCommand {
    public final static String command = "showallparticipants";
    private final boolean isInTextCommand = false;
    @Autowired
    ParticipantFacade participantsFacade;

    @Override
    public String command() {
        return command;
    }

    @Override
    protected String getTextForCallBackAnswer(Message message) {
        return Icon.ATHLETE + " Атлети " + Icon.ATHLETE;
    }

    @Override
    protected void perform(Message message) {
        String chatId = message.getChatId();
        String commandArgument = getCommandArgumentString(message.getMessageText());

        participantsFacade.showFindAllParticipants(chatId, commandArgument, message.getEditMessageId());
    }

    public static String getCallbackCommand(int page, String search) {
        return "/" + command + " {\"" + PAGE.getValue() + "\":\"" + page + "\",\"" + SEARCH.getValue() + "\":\"" + search + "\"}";
    }

    public static String getStateText(int page) {
        return "/" + command + " {\"" + PAGE.getValue() + "\":\"" + page + "\",\"" + SEARCH.getValue() + "\":\"{}\"}";
    }

}
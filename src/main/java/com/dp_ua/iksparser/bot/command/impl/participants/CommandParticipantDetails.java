package com.dp_ua.iksparser.bot.command.impl.participants;

import com.dp_ua.iksparser.bot.Icon;
import com.dp_ua.iksparser.bot.abilities.action.ActionType;
import com.dp_ua.iksparser.bot.abilities.participant.ParticipantFacade;
import com.dp_ua.iksparser.bot.command.BaseCommand;
import com.dp_ua.iksparser.bot.message.Message;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@ToString
public class CommandParticipantDetails extends BaseCommand {
    public final static String command = "showparticipantdetails";
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
        participantsFacade.showParticipantDetails(chatId, commandArgument, message.getEditMessageId());
    }

    public static String getCallbackCommand(int page, long id) {
        return "/" + command + " {\"page\":\"" + page + "\",\"id\":\"" + id + "\"}";
    }

    public static String getCallbackCommand(int page, long id, ActionType action) {
        return "/" + command + " {\"page\":\"" + page + "\",\"id\":\"" + id + "\",\"action\":\"" + action + "\"}";
    }
}
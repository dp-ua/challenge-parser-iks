package com.dp_ua.iksparser.bot.command.impl.participants;

import com.dp_ua.iksparser.bot.Icon;
import com.dp_ua.iksparser.bot.abilities.participant.ParticipantFacade;
import com.dp_ua.iksparser.bot.command.BaseCommand;
import com.dp_ua.iksparser.bot.message.Message;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@ToString
public class CommandParticipants extends BaseCommand {
    private final static String command = "participants";
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
        long commandArgument = getCommandArgument(message.getMessageText());
        participantsFacade.showParticipants(chatId, commandArgument, message.getEditMessageId());
    }

    public static String getCallbackCommand() {
        return "/" + command;
    }
}
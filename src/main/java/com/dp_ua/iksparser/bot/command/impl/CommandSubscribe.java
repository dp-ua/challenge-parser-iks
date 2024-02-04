package com.dp_ua.iksparser.bot.command.impl;

import com.dp_ua.iksparser.bot.Icon;
import com.dp_ua.iksparser.bot.abilities.ParticipantFacade;
import com.dp_ua.iksparser.bot.command.BaseCommand;
import com.dp_ua.iksparser.bot.message.Message;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@ToString
public class CommandSubscribe extends BaseCommand {
    public final static String command = "subscribe";
    private final boolean isInTextCommand = false;
    @Autowired
    private ParticipantFacade participantFacade;

    @Override
    public String command() {
        return command;
    }

    @Override
    protected String getTextForCallBackAnswer(Message message) {
        return Icon.SUBSCRIBE + " Підписуємось на спортсмена " + Icon.SUBSCRIBE;
    }

    @Override
    protected void perform(Message message) {
        String chatId = message.getChatId();
        long commandArgument = getCommandArgument(message.getMessageText());
        participantFacade.subscribe(chatId, commandArgument, message.getEditMessageId());
    }
}

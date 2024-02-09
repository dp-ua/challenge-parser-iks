package com.dp_ua.iksparser.bot.command.impl.subscribe;

import com.dp_ua.iksparser.bot.Icon;
import com.dp_ua.iksparser.bot.abilities.participant.ParticipantFacade;
import com.dp_ua.iksparser.bot.command.BaseCommand;
import com.dp_ua.iksparser.bot.message.Message;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@ToString
public class CommandUnsubscribe extends BaseCommand {
    public final static String command = "unsubscribe";
    private final boolean isInTextCommand = false;
    @Autowired
    private ParticipantFacade participantFacade;

    @Override
    public String command() {
        return command;
    }

    @Override
    protected String getTextForCallBackAnswer(Message message) {
        return Icon.UNSUBSCRIBE + " Відписуємось від спортсмена " + Icon.UNSUBSCRIBE;
    }

    @Override
    protected void perform(Message message) {
        String chatId = message.getChatId();
        long commandArgument = getCommandArgument(message.getMessageText());
        participantFacade.unsubscribe(chatId, commandArgument, message.getEditMessageId());
    }
}

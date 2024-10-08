package com.dp_ua.iksparser.bot.command.impl.subscribe;

import com.dp_ua.iksparser.bot.Icon;
import com.dp_ua.iksparser.bot.abilities.subscribe.SubscribeFacade;
import com.dp_ua.iksparser.bot.command.BaseCommand;
import com.dp_ua.iksparser.bot.message.Message;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@ToString
public class CommandSubscriptions extends BaseCommand {
    public final static String command = "subscriptions";
    protected static final String DESCRIPTION = Icon.STAR + " Підписки";
    @Autowired
    private SubscribeFacade subscribeFacade;

    @Override
    public boolean isNeedToAddToMenu() {
        return true;
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

    @Override
    public String command() {
        return command;
    }

    @Override
    protected String getTextForCallBackAnswer(Message message) {
        return DESCRIPTION;
    }

    @Override
    protected void perform(Message message) {
        String chatId = message.getChatId();
        long commandArgument = getCommandArgument(message.getMessageText());
        subscribeFacade.showSubscriptions(chatId, commandArgument, message.getEditMessageId());
    }
}
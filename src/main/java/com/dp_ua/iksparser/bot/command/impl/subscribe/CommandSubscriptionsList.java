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
public class CommandSubscriptionsList extends BaseCommand {
    public final static String command = "subscriptionslist";
    private final boolean isInTextCommand = false;
    @Autowired
    private SubscribeFacade subscribeFacade;

    @Override
    public String command() {
        return command;
    }

    @Override
    protected String getTextForCallBackAnswer(Message message) {
        return Icon.SUBSCRIBE + " Підписки " + Icon.SUBSCRIBE;
    }

    @Override
    protected void perform(Message message) {
        String chatId = message.getChatId();
        long commandArgument = getCommandArgument(message.getMessageText());
        subscribeFacade.showSubscriptionsList(chatId, commandArgument, message.getEditMessageId());
    }
}
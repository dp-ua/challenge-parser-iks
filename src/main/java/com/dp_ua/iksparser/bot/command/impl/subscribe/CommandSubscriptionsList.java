package com.dp_ua.iksparser.bot.command.impl.subscribe;

import com.dp_ua.iksparser.bot.Icon;
import com.dp_ua.iksparser.bot.abilities.subscribe.SubscribeFacade;
import com.dp_ua.iksparser.bot.command.BaseCommand;
import com.dp_ua.iksparser.bot.message.Message;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.dp_ua.iksparser.bot.command.CommandArgumentName.PAGE;


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
        Integer editMessageId = message.getEditMessageId();
        int page = getPage(message);

        subscribeFacade.showSubscriptionsList(chatId, page, editMessageId);
    }

    private int getPage(Message message) {
        try {
            return Integer.parseInt(parseArgumentFromFullText(message.getMessageText(), PAGE));
        } catch (Exception e) {
            return DEFAULT_START_PAGE;
        }
    }

    public static String getCallBackCommand(int page) {
        return SLASH + command +
                BRACKET_OPEN +
                paramPage(page) +
                BRACKET_CLOSE;
    }
}
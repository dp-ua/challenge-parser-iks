package com.dp_ua.iksparser.bot.command;

import com.dp_ua.iksparser.bot.abilities.action.ActionController;
import com.dp_ua.iksparser.bot.event.SendMessageEvent;
import com.dp_ua.iksparser.bot.message.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import static com.dp_ua.iksparser.service.MessageCreator.SERVICE;

@Slf4j
public abstract class BaseCommand implements CommandInterface {
    public static final int DEFAULT_NO_PAGE_ARGUMENT = -1;

    @Autowired
    protected ApplicationEventPublisher publisher;
    @Autowired
    ActionController actionController;

    protected abstract String getTextForCallBackAnswer(Message message);

    protected abstract void perform(Message message);

    @Override
    public void execute(Message message) {
        sendCallbackForMessage(message, getTextForCallBackAnswer(message));
        performAction(message);
        perform(message);
    }

    private void performAction(Message message) {
        try {
            String arguments = getCommandArgumentString(message.getMessageText());
            actionController.getActionType(arguments).ifPresent(actionType -> {
                actionController.performAction(actionType, message.getChatId(), arguments);
            });
        } catch (Exception e) {
            log.error("Error in performAction: {}", e.getMessage());
        }
    }

    protected void sendCallbackForMessage(Message message, String text) {
        if (message.hasCallbackQuery()) {
            String callBackQueryId = message.getCallBackQueryId();
            SendMessageEvent answerCallbackQuery = SERVICE.getAnswerCallbackQuery(callBackQueryId, text);
            if (answerCallbackQuery != null) {
                log.info("answerCallbackQuery: {}", answerCallbackQuery);
                publisher.publishEvent(answerCallbackQuery);
            }
        }
    }

    protected int getCommandArgument(String text) {
        if (text.startsWith("/" + command())) {
            String argument = text.substring(command().length() + 1).trim();
            return argument.isEmpty() ? DEFAULT_NO_PAGE_ARGUMENT : Integer.parseInt(argument);
        }
        return 0;
    }

    protected String getCommandArgumentString(String text) {
        if (text.startsWith("/" + command())) {
            String argument = text.substring(command().length() + 1).trim();
            return argument.isEmpty() ? "" : argument;
        }
        return "";
    }
}

package com.dp_ua.iksparser.bot.command;

import com.dp_ua.iksparser.bot.message.Message;
import com.dp_ua.iksparser.bot.performer.event.SendMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import static com.dp_ua.iksparser.service.MessageCreator.SERVICE;

@Slf4j
public abstract class BaseCommand implements CommandInterface {
    @Autowired
    ApplicationEventPublisher publisher;

    private void sendCallbackMessage(String callBackQueryId, String message) {
        SendMessageEvent answerCallbackQuery = SERVICE.getAnswerCallbackQuery(callBackQueryId, message);
        if (answerCallbackQuery != null) {
            log.info("answerCallbackQuery: {}", answerCallbackQuery);
            publisher.publishEvent(answerCallbackQuery);
        }
    }

    @Override
    public void execute(Message message) {
        if (message.hasCallbackQuery()) {
            sendCallbackMessage(message.getCallBackQueryId(), getTextForCallBackAnswer(message));
        }
        perform(message);
    }

    protected abstract String getTextForCallBackAnswer(Message message);

    protected abstract void perform(Message message);

    public int getCommandArgument(String text) {
        if (text.startsWith("/" + command())) {
            String argument = text.substring(command().length() + 1).trim();
            return argument.isEmpty() ? 0 : Integer.parseInt(argument);
        }
        return 0;
    }
    public String getCommandArgumentString(String text) {
        if (text.startsWith("/" + command())) {
            String argument = text.substring(command().length() + 1).trim();
            return argument.isEmpty() ? "" : argument;
        }
        return "";
    }
}

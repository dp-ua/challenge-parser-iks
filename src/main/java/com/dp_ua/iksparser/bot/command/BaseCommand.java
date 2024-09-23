package com.dp_ua.iksparser.bot.command;

import com.dp_ua.iksparser.bot.abilities.action.ActionController;
import com.dp_ua.iksparser.bot.abilities.action.ActionType;
import com.dp_ua.iksparser.bot.abilities.response.ResponseContainer;
import com.dp_ua.iksparser.bot.abilities.response.ResponseContentFactory;
import com.dp_ua.iksparser.bot.abilities.response.ResponseContentGenerator;
import com.dp_ua.iksparser.bot.event.SendMessageEvent;
import com.dp_ua.iksparser.bot.message.Message;
import com.dp_ua.iksparser.service.JsonReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import static com.dp_ua.iksparser.bot.abilities.response.ResponseType.NOT_IMPLEMENTED;
import static com.dp_ua.iksparser.bot.command.CommandArgumentName.*;
import static com.dp_ua.iksparser.service.MessageCreator.SERVICE;

@Slf4j
public abstract class BaseCommand implements CommandInterface {
    public static final int DEFAULT_NO_PAGE_ARGUMENT = -1;
    protected static final String PARAM_DELIMITER = ",";
    protected static final String BRACKET_OPEN = " {";
    protected static final String BRACKET_CLOSE = "}";
    protected static final String SLASH = "/";

    @Autowired
    protected ApplicationEventPublisher publisher;
    @Autowired
    ActionController actionController;
    @Autowired
    ResponseContentFactory responseContentFactory;
    @Autowired
    JsonReader jSonReader;

    protected abstract String getTextForCallBackAnswer(Message message);

    protected abstract void perform(Message message);

    @Override
    public void execute(Message message) {
        try {
            sendCallbackForMessage(message, getTextForCallBackAnswer(message));
            performAction(message);
            perform(message);
        } catch (NotImplementedException e) {
            log.error("Not implemented yet: {}", e.getMessage());
            informUserAboutNotImplemented(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void informUserAboutNotImplemented(Message message) {
        String chatId = message.getChatId();
        ResponseContentGenerator generator = responseContentFactory.getContentForResponse(NOT_IMPLEMENTED);
        ResponseContainer container = generator.getContainer();

        SendMessageEvent sendMessageEvent = SERVICE.getSendMessageEvent(chatId, null, container);
        publisher.publishEvent(sendMessageEvent);
    }

    private void performAction(Message message) {
        try {
            String arguments = getCommandArgumentString(message.getMessageText());
            actionController.getActionType(arguments).ifPresent(actionType ->
                    actionController.performAction(actionType, message.getChatId(), arguments)
            );
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
        String argument = getCommandArgumentString(text);
        return argument.isEmpty() ? DEFAULT_NO_PAGE_ARGUMENT : Integer.parseInt(argument);
    }

    protected String parseArgumentFromFullText(String text, CommandArgumentName argumentName) {
        String arguments = getCommandArgumentString(text);
        if (arguments.startsWith("{")) {
            return jSonReader.getVal(arguments, argumentName.getValue());  // pass the arguments, not text
        }
        throw new RuntimeException("Argument not found");
    }

    protected String getCommandArgumentString(String text) {
        String commandPattern = "/" + command() + "(?:@[\\w_]+)?"; // Команда может заканчиваться суффиксом @Bot_name
        if (text.matches(commandPattern + "(\\s|\\n).*")) { // Учитываем пробел или символ новой строки как разделитель
            String argument = text.replaceFirst(commandPattern, "").trim(); // Убираем команду и ее суффикс, если есть
            return argument.isEmpty() ? "" : argument;
        }
        return "";
    }

    protected static String paramParticipant(long participantId) {
        return paramObject(PARTICIPANT_ID, String.valueOf(participantId));
    }

    protected static String paramCompetition(long competitionId) {
        return paramObject(COMPETITION_ID, String.valueOf(competitionId));
    }

    protected static String paramAction(ActionType action) {
        return paramObject(ACTION, action.toString());
    }

    protected static String paramObject(CommandArgumentName name, String value) {
        return "\"" + name.getValue() + "\":\"" + value + "\"";
    }
}

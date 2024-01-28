package com.dp_ua.iksparser.bot.performer;

import com.dp_ua.iksparser.bot.Bot;
import com.dp_ua.iksparser.bot.event.SendMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
public class SendMessagePerformer implements ApplicationListener<SendMessageEvent> {
    @Autowired
    Bot bot;

    @Override
    @Async
    public void onApplicationEvent(SendMessageEvent event) {
        log.info("SendMessageEvent. Type: {}, Message:{}",
                event.getMsgType(),
                event.getMessage().toString().replaceAll("\n", " "));
        Message result = null;
        try {
            switch (event.getMsgType()) {
                case SEND_MESSAGE:
                    result = bot.execute((SendMessage) event.getMessage());
                    break;
                    case EDIT_MESSAGE:
                    bot.execute((EditMessageText)event.getMessage());
                    break;
                case CHAT_ACTION:
                    bot.execute((SendChatAction) event.getMessage());
                    break;
                case ANSWER_CALLBACK_QUERY:
                    bot.execute((AnswerCallbackQuery) event.getMessage());
                    break;
                default:
                    throw new IllegalArgumentException("Unknown msgType: " + event.getMsgType());
            }
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        if (result != null) {
            log.info("Message sent. Result: {}", result.toString().replaceAll("\n", " "));
            // todo тут можно фиксировать результаты отправки сообщений
            /*   возможно необходимо будет сохранять айди сообщений для последующего удаления/редактирования
             */
        }
    }
}

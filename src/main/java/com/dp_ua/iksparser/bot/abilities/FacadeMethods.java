package com.dp_ua.iksparser.bot.abilities;

import com.dp_ua.iksparser.bot.abilities.response.ResponseContentFactory;
import com.dp_ua.iksparser.bot.event.SendMessageEvent;
import com.dp_ua.iksparser.service.JsonReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;

import static com.dp_ua.iksparser.bot.event.SendMessageEvent.MsgType.CHAT_ACTION;
import static com.dp_ua.iksparser.service.MessageCreator.SERVICE;

@Slf4j
public class FacadeMethods {
    @Autowired
    protected ApplicationEventPublisher publisher;
    @Autowired
    protected StateService stateService;
    @Autowired
    protected ResponseContentFactory contentFactory;
    @Autowired
    protected JsonReader jSonReader;

    protected void sendTypingAction(String chatId) {
        log.info("sendTyping for chat: {}", chatId);
        SendChatAction chatAction = SERVICE.getChatAction(chatId);
        publisher.publishEvent(new SendMessageEvent(this, chatAction, CHAT_ACTION));
    }

    protected void validate(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }
}

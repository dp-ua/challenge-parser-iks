package com.dp_ua.iksparser.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.dp_ua.iksparser.bot.event.GetMessageEvent;
import com.dp_ua.iksparser.bot.message.ReceivedMessage;

import jakarta.validation.constraints.NotEmpty;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@EqualsAndHashCode(callSuper = true)
public class Bot extends TelegramLongPollingBot {
    @NotEmpty
    private final String botUserName;
    @Autowired
    ApplicationEventPublisher publisher;

    @Override
    public void onUpdateReceived(Update update) {
        ReceivedMessage message = new ReceivedMessage(update);
        log.info("IN chat={} user={} text={}", message.getChatId(), message.getUserName(),
                abbreviate(message.getMessageText()));
        log.debug("Update: {}", update.toString().replace("\n", " "));
        GetMessageEvent messageEvent = new GetMessageEvent(this, message);
        publisher.publishEvent(messageEvent);
    }

    private static String abbreviate(String text) {
        if (text == null) {
            return null;
        }
        String s = text.replace("\n", " ");
        return s.length() > 80 ? s.substring(0, 80) + "..." : s;
    }

    public Bot(String botToken, String botUserName) {
        super(botToken);
        this.botUserName = botUserName;
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }
}

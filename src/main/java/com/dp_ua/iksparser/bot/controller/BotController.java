package com.dp_ua.iksparser.bot.controller;

import static com.dp_ua.iksparser.service.MessageCreator.SERVICE;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;

import com.dp_ua.iksparser.bot.Bot;
import com.dp_ua.iksparser.bot.event.SendMessageEvent;
import com.dp_ua.iksparser.configuration.TelegramBotProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class BotController implements ControllerService {

    private final ApplicationEventPublisher publisher;
    private final Bot bot;
    private final TelegramBotsApi telegramBotsApi;
    private final TelegramBotProperties botProperties;

    public BotSession botConnect() {
        try {
            BotSession botSession = telegramBotsApi.registerBot(bot);
            log.info("TelegramAPI started. Look for messages");
            sendMessageToAdmin("TelegramAPI started. Look for messages");
            return botSession;
        } catch (TelegramApiException e) {
            log.error("Cant Connect. Pause {}sec and I'll try again. Error: {}", getTimeInSec(), e.getMessage());
            try {
                Thread.sleep(botProperties.getReconnectTimeout());
            } catch (InterruptedException e1) {
                log.error("Error while sleep", e1);
                return null;
            }
            botConnect();
        }
        return null;
    }

    public void sendMessageToAdmin(String text) {
        SendMessage message = SERVICE.getSendMessage(botProperties.getAdminId(), text);
        SendMessageEvent event = new SendMessageEvent(this, message, SendMessageEvent.MsgType.SEND_MESSAGE);
        publisher.publishEvent(event);
    }

    private long getTimeInSec() {
        return botProperties.getReconnectTimeout() / 1000;
    }

    public void sendMessageToUser(String chatId, String text) {
        log.info("Send message to chatId: {} with text: {}", chatId, text);
        SendMessage message = SERVICE.getSendMessage(chatId, text);
        SendMessageEvent event = new SendMessageEvent(this, message, SendMessageEvent.MsgType.SEND_MESSAGE);
        publisher.publishEvent(event);
    }

}

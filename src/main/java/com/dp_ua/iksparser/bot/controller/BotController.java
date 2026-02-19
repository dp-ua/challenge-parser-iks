package com.dp_ua.iksparser.bot.controller;

import static com.dp_ua.iksparser.service.MessageCreator.SERVICE;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;

import com.dp_ua.iksparser.bot.Bot;
import com.dp_ua.iksparser.bot.event.SendMessageEvent;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class BotController implements ControllerService {

    @Value("${telegram.bot.reconnectTimeout}")
    private int reconnectTimeout;
    @Value("${telegram.bot.adminId}")
    @Getter
    private String adminId;

    private final ApplicationEventPublisher publisher;
    private final Bot bot;
    private final TelegramBotsApi telegramBotsApi;

    public BotSession botConnect() {
        try {
            BotSession botSession = telegramBotsApi.registerBot(bot);
            log.info("TelegramAPI started. Look for messages");
            sendMessageToAdmin("TelegramAPI started. Look for messages");
            return botSession;
        } catch (TelegramApiException e) {
            log.error("Cant Connect. Pause {}sec and I'll try again. Error: {}", getTimeInSec(), e.getMessage());
            try {
                Thread.sleep(reconnectTimeout);
            } catch (InterruptedException e1) {
                log.error("Error while sleep", e1);
                return null;
            }
            botConnect();
        }
        return null;
    }

    public void sendMessageToAdmin(String text) {
        SendMessage message = SERVICE.getSendMessage(adminId, text);
        SendMessageEvent event = new SendMessageEvent(this, message, SendMessageEvent.MsgType.SEND_MESSAGE);
        publisher.publishEvent(event);
    }

    private int getTimeInSec() {
        return reconnectTimeout / 1000;
    }

    public void sendMessageToUser(String chatId, String text) {
        log.info("Send message to chatId: {} with text: {}", chatId, text);
        SendMessage message = SERVICE.getSendMessage(chatId, text);
        SendMessageEvent event = new SendMessageEvent(this, message, SendMessageEvent.MsgType.SEND_MESSAGE);
        publisher.publishEvent(event);
    }

}

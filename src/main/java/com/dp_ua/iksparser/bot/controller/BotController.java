package com.dp_ua.iksparser.bot.controller;

import com.dp_ua.iksparser.bot.Bot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;

@Component
@Slf4j
public class BotController implements ControllerService {
    @Value("${telegram.bot.reconnectTimeout}")
    private int reconnectTimeout;
    @Autowired
    Bot bot;
    @Autowired
    TelegramBotsApi telegramBotsApi;

    public BotSession botConnect() {
        try {
            BotSession botSession = telegramBotsApi.registerBot(bot);
            log.info("TelegramAPI started. Look for messages");
            return botSession;
        } catch (TelegramApiException e) {
            log.error("Cant Connect. Pause " + getTimeInSec() + "sec and I'll try again. Error: " + e.getMessage());
            try {
                Thread.sleep(reconnectTimeout);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
                return null;
            }
            botConnect();
        }
        return null;
    }

    private int getTimeInSec() {
        return reconnectTimeout / 1000;
    }
}

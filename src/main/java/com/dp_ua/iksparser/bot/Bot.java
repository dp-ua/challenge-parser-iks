package com.dp_ua.iksparser.bot;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.validation.constraints.NotEmpty;


@Slf4j
@EqualsAndHashCode(callSuper = true)
public class Bot extends TelegramLongPollingBot {
    @NotEmpty
    private final String botUserName;

    @Override
    public void onUpdateReceived(Update update) {
        log.info("onUpdateReceived: " + update.toString());
        // TODO: 2021-10-13
        // Add JMS message to queue
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

package com.dp_ua.iksparser.bot;

import com.dp_ua.iksparser.bot.message.ReceivedMessage;
import com.dp_ua.iksparser.bot.performer.event.GetMessageEvent;
import jakarta.validation.constraints.NotEmpty;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;


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
        log.info("Message: " + message + " Publish Update: " + update.toString().replace("\n", " "));
        GetMessageEvent messageEvent = new GetMessageEvent(this, message);
        publisher.publishEvent(messageEvent);
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

package com.dp_ua.iksparser.bot.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Objects;

public class ReceivedMessage implements Message {
    protected final static Logger log = LoggerFactory.getLogger(ReceivedMessage.class);
    private final Update update;

    public ReceivedMessage(Update update) throws NullPointerException {
        this.update = Objects.requireNonNull(update, "Update can't be Null");
        log.trace("Receive message: " + update);
    }

    @Override
    public String getChatId() {
        return "";
    }

    @Override
    public String getUserId() {
        return "";
    }

    @Override
    public Type getType() {
        return Type.RECEIVE;
    }

    @Override
    public String getMessageText() {
        return "";
    }
}

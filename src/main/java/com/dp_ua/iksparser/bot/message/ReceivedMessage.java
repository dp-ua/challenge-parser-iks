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
        if (update.hasMessage()) {
            return update.getMessage().getChatId().toString();
        }
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId().toString();
        }
        return "";
    }

    @Override
    public String getUserId() {
        if (update.hasMessage()) {
            return update.getMessage().getFrom().getId().toString();
        }
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getFrom().getId().toString();
        }
        return "";
    }

    @Override
    public Type getType() {
        return Type.RECEIVE;
    }

    @Override
    public boolean hasCallbackQuery() {
        return update.hasCallbackQuery();
    }

    @Override
    public String getCallBackQueryId() {
        return update.getCallbackQuery().getId();
    }

    @Override
    public Integer getEditMessageId() {
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getMessageId();
        }
        return null;
    }

    @Override
    public String getMessageText() {
        if (update.hasMessage()) {
            return update.getMessage().getText();
        }
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getData();
        }
        return "";
    }
}

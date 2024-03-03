package com.dp_ua.iksparser.bot.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;

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
        if (update.hasChatMember()) {
            return update.getChatMember().getChat().getId().toString();
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
    public String getUserName() {
        User user = getUser();
        if (user != null) {
            return user.getFirstName() + " " + user.getLastName() + "[@" + user.getUserName() + "]";
        }
        return "";
    }

    @Override
    public boolean kickBot() {
        if (update.hasMyChatMember()) {
            ChatMemberUpdated chatMember = update.getMyChatMember();
            ChatMember newChatMember = chatMember.getNewChatMember();
            if ("kicked".equals(newChatMember.getStatus())) {
                return true;
            }
            if (!(newChatMember == null)) {
                log.warn("ChatMemberUpdated: " + newChatMember.getStatus() + " " + newChatMember.getUser().getUserName());
            }
        }
        return false;
    }

    private User getUser() {
        if (update.hasMessage()) {
            return update.getMessage().getFrom();
        }
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getFrom();
        }
        if (update.hasChatMember()) {
            return update.getChatMember().getFrom();
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
        return null;
    }
}

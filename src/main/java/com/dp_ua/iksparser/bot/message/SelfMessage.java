package com.dp_ua.iksparser.bot.message;

import lombok.Getter;
import lombok.Setter;

@Getter
public class SelfMessage implements Message {
    @Setter
    private String messageText;
    @Setter
    private String chatId;
    @Setter
    private Integer editMessageId;

    @Override
    public String getUserId() {
        return null;
    }

    @Override
    public Type getType() {
        return Type.SELF;
    }

    @Override
    public boolean hasCallbackQuery() {
        return editMessageId != null;
    }

    @Override
    public String getCallBackQueryId() {
        return null;
    }

    @Override
    public String getUserName() {
        return "SelfUserName";
    }

    @Override
    public boolean kickBot() {
        return false;
    }
}

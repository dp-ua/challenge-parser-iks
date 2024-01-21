package com.dp_ua.iksparser.bot.message;

import lombok.Getter;
import lombok.Setter;

public class SelfMessage implements Message {
    @Getter
    @Setter
    private String messageText;
    @Getter
    @Setter
    private String chatId;
    @Getter
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
}

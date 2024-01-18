package com.dp_ua.iksparser.bot.message;

import javax.validation.constraints.NotNull;

public interface Message {
    @NotNull
    String getMessageText();

    @NotNull
    String getChatId();

    @NotNull
    String getUserId();

    Type getType();

    boolean hasCallbackQuery();

    String getCallBackQueryId();

    Integer getEditMessageId();

    enum Type {
        RECEIVE, SELF
    }
}

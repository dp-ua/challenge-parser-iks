package com.dp_ua.iksparser.bot.message;

import javax.validation.constraints.NotNull;

public interface Message{
    @NotNull
    String getMessageText();

    @NotNull
    String getChatId();

    @NotNull
    String getUserId();

    Type getType();

    enum Type {
        RECEIVE, SEND, SELF
    }

}

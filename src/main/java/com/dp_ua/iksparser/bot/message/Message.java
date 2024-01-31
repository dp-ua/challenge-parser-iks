package com.dp_ua.iksparser.bot.message;

public interface Message {
    String getMessageText();

    String getChatId();

    String getUserId();

    Type getType();

    boolean hasCallbackQuery();

    String getCallBackQueryId();

    Integer getEditMessageId();

    String getUserName();
    enum Type {
        RECEIVE, SELF
    }
}

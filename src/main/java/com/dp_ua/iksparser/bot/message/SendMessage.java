package com.dp_ua.iksparser.bot.message;

public class SendMessage implements Message{
    @Override
    public String getMessageText() {
        return null;
    }

    @Override
    public String getChatId() {
        return null;
    }

    @Override
    public String getUserId() {
        return null;
    }

    @Override
    public Type getType() {
        return Type.SEND;
    }
}

package com.dp_ua.iksparser.bot.performer.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class SendMessageEvent extends ApplicationEvent {
    @Getter
    private final Object message;
    @Getter
    private final MsgType msgType;

    public SendMessageEvent(Object source, Object message, MsgType msgType) {
        super(source);
        this.message = message;
        this.msgType = msgType;
    }

    public enum MsgType {
        SEND_MESSAGE, CHAT_ACTION, EDIT_MESSAGE, ANSWER_CALLBACK_QUERY
    }
}

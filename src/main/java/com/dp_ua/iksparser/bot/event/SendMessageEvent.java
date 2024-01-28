package com.dp_ua.iksparser.bot.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SendMessageEvent extends ApplicationEvent {
    private final Object message;
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

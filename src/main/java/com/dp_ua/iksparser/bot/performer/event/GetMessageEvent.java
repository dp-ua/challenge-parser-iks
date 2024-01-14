package com.dp_ua.iksparser.bot.performer.event;

import com.dp_ua.iksparser.bot.message.Message;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class GetMessageEvent extends ApplicationEvent {
    @Getter
    private final Message message;

    public GetMessageEvent(Object source, Message message) {
        super(source);
        this.message = message;

    }
}

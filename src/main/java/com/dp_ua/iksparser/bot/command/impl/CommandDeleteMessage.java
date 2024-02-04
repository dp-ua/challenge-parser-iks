package com.dp_ua.iksparser.bot.command.impl;

import com.dp_ua.iksparser.bot.abilities.StateService;
import com.dp_ua.iksparser.bot.command.CommandInterface;
import com.dp_ua.iksparser.bot.event.SendMessageEvent;
import com.dp_ua.iksparser.bot.message.Message;
import com.dp_ua.iksparser.service.MessageCreator;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;


@Component
@ToString
@Slf4j
public class CommandDeleteMessage implements CommandInterface {
    public static final String command = "deletemessage";
    private final boolean isInTextCommand = false;
    @Autowired
    ApplicationEventPublisher publisher;
    @Autowired
    StateService stateService;

    @Override
    public String command() {
        return command;
    }

    @Override
    public void execute(Message message) {
        Integer editMessageId = message.getEditMessageId();
        log.info("Delete message with id: {}", editMessageId);
        DeleteMessage deleteMessage = MessageCreator.SERVICE.getDeleteMessage(message.getChatId(), editMessageId);
        SendMessageEvent sendMessageEvent = new SendMessageEvent(this, deleteMessage, SendMessageEvent.MsgType.DELETE_MESSAGE);
        publisher.publishEvent(sendMessageEvent);
        stateService.resetState(message.getChatId());
    }
}

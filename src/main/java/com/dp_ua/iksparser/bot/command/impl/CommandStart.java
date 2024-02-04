package com.dp_ua.iksparser.bot.command.impl;

import com.dp_ua.iksparser.bot.Bot;
import com.dp_ua.iksparser.bot.command.CommandInterface;
import com.dp_ua.iksparser.bot.message.Message;
import com.dp_ua.iksparser.bot.event.SendMessageEvent;
import com.dp_ua.iksparser.service.MessageCreator;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static com.dp_ua.iksparser.bot.Icon.*;
import static com.dp_ua.iksparser.bot.event.SendMessageEvent.MsgType.SEND_MESSAGE;
import static com.dp_ua.iksparser.service.MessageCreator.*;

@Component
@ToString
@EqualsAndHashCode
public class CommandStart implements CommandInterface {
    public static final String command = "start";
    private final boolean isInTextCommand = false;
    @Autowired
    private Bot bot;
    @Autowired
    private ApplicationEventPublisher publisher;

    @Override
    public String command() {
        return command;
    }

    @Override
    public void execute(Message message) {
        StringBuilder sb = getStartMessageText();
        sendMessage(message, sb);
    }

    private StringBuilder getStartMessageText() {
        StringBuilder sb = new StringBuilder();
        sb
                .append("Вітаю, мене звуть ")
                .append(LINK)
                .append("AthleteSearch ")
                .append(ROBOT)
                .append(LINK_END)
                .append(LINK_SEPARATOR)
                .append("https://t.me/Athletesearch_bot")
                .append(LINK_SEPARATOR_END)
                .append(END_LINE)
                .append("Я допоможу тобі знайти інформацію про змагання")
                .append(EVENT)
                .append(", спортсменів")
                .append(ATHLETE)
                .append(" та тренерів")
                .append(COACH)
                .append(END_LINE)
                .append(END_LINE)
                .append("Для початку роботи введи команду /" + CommandCompetitions.command)
                .append(END_LINE)
                .append("Або скористайся кнопкою в меню");
        return sb;
    }

    private void sendMessage(Message message, StringBuilder sb) {
        String chatId = message.getChatId();
        SendMessage sendMessage = MessageCreator.SERVICE.getSendMessage(
                chatId,
                sb.toString(),
                null,
                true);
        SendMessageEvent sendMessageEvent = new SendMessageEvent(
                this,
                sendMessage,
                SEND_MESSAGE);
        publisher.publishEvent(sendMessageEvent);
    }
}

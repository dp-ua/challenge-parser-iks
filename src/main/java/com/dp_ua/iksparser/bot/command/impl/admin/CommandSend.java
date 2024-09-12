package com.dp_ua.iksparser.bot.command.impl.admin;

import com.dp_ua.iksparser.bot.command.AdminAccessCommand;
import com.dp_ua.iksparser.bot.controller.BotController;
import com.dp_ua.iksparser.bot.message.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CommandSend extends AdminAccessCommand {
    @Autowired
    BotController bot;

    private static final String command = "send";

    @Override
    protected String getTextForCallBackAnswer(Message message) {
        return null;
    }

    @Override
    protected void perform(Message message) {
        String textWithChatId = getCommandArgumentString(message.getMessageText());
        String chatId = parseChatIdFromArgument(textWithChatId);
        if (chatId == null) {
            log.warn("Empty chatId for send command");
            return;
        }
        String text = getTextWithoutChatId(textWithChatId, chatId);
        if (!text.isEmpty()) {
            bot.sendMessageToUser(chatId, text);
        }
    }

    private String getTextWithoutChatId(String textWithChatId, String chatId) {
        return textWithChatId.replaceFirst(chatId, "").trim();
    }

    private String parseChatIdFromArgument(String text) {
        log.debug("parse chatId from argument: {}", text);
        String[] s = text.split(" ");
        if (s.length > 1) {
            return s[0];
        }
        return null;
    }

    @Override
    public String command() {
        return command;
    }
}
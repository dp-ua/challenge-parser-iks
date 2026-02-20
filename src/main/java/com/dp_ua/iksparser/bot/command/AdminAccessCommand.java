package com.dp_ua.iksparser.bot.command;

import org.springframework.beans.factory.annotation.Autowired;

import com.dp_ua.iksparser.bot.Icon;
import com.dp_ua.iksparser.bot.message.Message;
import com.dp_ua.iksparser.configuration.TelegramBotProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AdminAccessCommand extends BaseCommand {

    @Autowired
    TelegramBotProperties botProperties;

    @Override
    public void execute(Message message) {
        String chatId = message.getChatId();
        if (!isAdmin(chatId)) {
            log.info("Access denied for chatId: {}", chatId);
            sendCallbackForMessage(message, Icon.DENIED + " Access denied " + Icon.DENIED);
            return;
        }
        super.execute(message);
    }

    private boolean isAdmin(String chatId) {
        log.debug("Checking admin access for chatId: {}", chatId);
        return chatId != null && botProperties.isAdmin(chatId);
    }

}
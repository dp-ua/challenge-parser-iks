package com.dp_ua.iksparser.bot.command;

import com.dp_ua.iksparser.bot.Icon;
import com.dp_ua.iksparser.bot.controller.BotController;
import com.dp_ua.iksparser.bot.message.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class AdminAccessCommand extends BaseCommand {
    @Autowired
    BotController bot;

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
        log.debug("check admin access for chatId: {}", chatId);
        if (chatId == null) return false;
        if (bot.getAdminId() != null) {
            return chatId.equals(bot.getAdminId());
        }
        return false;
    }
}

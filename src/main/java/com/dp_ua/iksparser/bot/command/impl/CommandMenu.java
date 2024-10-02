package com.dp_ua.iksparser.bot.command.impl;

import com.dp_ua.iksparser.bot.abilities.MainFacade;
import com.dp_ua.iksparser.bot.command.BaseCommand;
import com.dp_ua.iksparser.bot.message.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CommandMenu extends BaseCommand {
    public static final String command = "menu";
    public static final String description = "Меню";
    @Autowired
    MainFacade mainFacade;

    @Override
    public String description() {
        return description;
    }

    @Override
    public boolean isNeedToAddToMenu() {
        return true;
    }

    @Override
    protected String getTextForCallBackAnswer(Message message) {
        return null;
    }

    @Override
    protected void perform(Message message) {
        log.info("Show menu for chatId: {}", message.getChatId());
        String argument = getCommandArgumentString(message.getMessageText());
        mainFacade.menu(message.getChatId(), argument, message.getEditMessageId());
    }

    @Override
    public String command() {
        return command;
    }

    public static String getCallBackCommand() {
        return "/" + command;
    }
}

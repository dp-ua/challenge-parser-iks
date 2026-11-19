package com.dp_ua.iksparser.bot.command.impl;

import org.springframework.stereotype.Component;

import com.dp_ua.iksparser.bot.abilities.MainFacade;
import com.dp_ua.iksparser.bot.command.CommandInterface;
import com.dp_ua.iksparser.bot.message.Message;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Component
@ToString
@EqualsAndHashCode
@Slf4j
@RequiredArgsConstructor
public class CommandStart implements CommandInterface {

    public static final String command = "start";
    public static final String DESCRIPTION = "Розпочати роботу";

    private final MainFacade mainFacade;

    @Override
    public String description() {
        return DESCRIPTION;
    }

    @Override
    public boolean isNeedToAddToMenu() {
        return true;
    }


    @Override
    public String command() {
        return command;
    }

    @Override
    public void execute(Message message) {
        log.info("Start command. ChatId: {}", message.getChatId());
        mainFacade.start(message.getChatId());
    }

}

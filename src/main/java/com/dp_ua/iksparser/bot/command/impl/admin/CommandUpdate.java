package com.dp_ua.iksparser.bot.command.impl.admin;

import com.dp_ua.iksparser.bot.abilities.competition.CompetitionFacade;
import com.dp_ua.iksparser.bot.command.AdminAccessCommand;
import com.dp_ua.iksparser.bot.controller.BotController;
import com.dp_ua.iksparser.bot.message.Message;
import com.dp_ua.iksparser.exeption.ParsingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CommandUpdate extends AdminAccessCommand {
    private static final String command = "update";
    @Autowired
    CompetitionFacade competitionFacade;
    @Autowired
    BotController bot;

    @Override
    protected String getTextForCallBackAnswer(Message message) {
        return null;
    }

    @Override
    protected void perform(Message message) {
        String argument = getCommandArgumentString(message.getMessageText());
        if (argument.isEmpty()) {
            log.warn("Empty argument for update command");
            return;
        }
        int year = Integer.parseInt(argument);
        if (year < 2000 || year > 3000) {
            log.warn("Invalid year for update command: {}", year);
            return;
        }
        try {
            log.info("Updating competitions list for year: {}", year);
            bot.sendMessageToUser(message.getChatId(), "Updating competitions list for year: " + year);
            competitionFacade.updateCompetitionsList(year);
        } catch (ParsingException e) {
            log.error("Error while updating competitions list", e);
        }
    }

    @Override
    public String command() {
        return command;
    }
}
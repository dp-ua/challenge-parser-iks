package com.dp_ua.iksparser.bot.command.impl.competition;

import com.dp_ua.iksparser.bot.Icon;
import com.dp_ua.iksparser.bot.abilities.competition.CompetitionFacade;
import com.dp_ua.iksparser.bot.command.BaseCommand;
import com.dp_ua.iksparser.bot.message.Message;
import com.dp_ua.iksparser.exeption.ParsingException;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@ToString
public class CommandCompetitions extends BaseCommand {
    public static final String command = "competitions";
    public static final String description = "Список змагань";
    private final boolean isInTextCommand = false;
    @Autowired
    private CompetitionFacade competitionFacade;

    @Override
    public String description() {
        return description;
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
    protected String getTextForCallBackAnswer(Message message) {
        return Icon.PREVIOUS + " дивитись змагання " + Icon.NEXT;
    }

    @Override
    protected void perform(Message message) {
        String chatId = message.getChatId();
        int commandArgument = getCommandArgument(message.getMessageText());
        try {
            competitionFacade.showCompetitions(chatId, commandArgument, message.getEditMessageId());
        } catch (ParsingException e) {
            throw new RuntimeException(e);
        }
    }
}

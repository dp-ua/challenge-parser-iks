package com.dp_ua.iksparser.bot.command.impl.competition;

import com.dp_ua.iksparser.bot.Icon;
import com.dp_ua.iksparser.bot.abilities.competition.CompetitionFacade;
import com.dp_ua.iksparser.bot.command.BaseCommand;
import com.dp_ua.iksparser.bot.message.Message;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.dp_ua.iksparser.bot.command.CommandArgumentName.PAGE;


@Component
@ToString
public class CommandCompetitions extends BaseCommand {
    private static final String command = "competitions";
    private static final String description = Icon.COMPETITION + " Список змагань";
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
        int page = getPage(message);
        competitionFacade.showCompetitions(chatId, page, message.getEditMessageId());
    }

    private int getPage(Message message) {
        try {
            return Integer.parseInt(parseArgumentFromFullText(message.getMessageText(), PAGE));
        } catch (Exception e) {
            return DEFAULT_NO_PAGE_ARGUMENT;
        }
    }

    public static String getCallbackCommand(int page) {
        return SLASH +
                command +
                BRACKET_OPEN +
                paramPage(page) +
                BRACKET_CLOSE;
    }

    public static String getCallbackCommand() {
        return getCallbackCommand(DEFAULT_NO_PAGE_ARGUMENT);
    }
}

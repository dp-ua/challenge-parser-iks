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
        int page = getPage(message);
        competitionFacade.showCompetitions(chatId, page, message.getEditMessageId());
    }

    private int getPage(Message message) {
        try {
            return Integer.parseInt(parseArgument(message.getMessageText(), PAGE.getValue()));
        } catch (Exception e) {
            return DEFAULT_NO_PAGE_ARGUMENT;
        }
    }

    public static String getCallbackCommand(long page) {
        return "/" + command + " {\"" + PAGE.getValue() + "\":\"" + page + "\"}";
    }

    public static String getCallbackCommand() {
        return "/" + command;
    }
}

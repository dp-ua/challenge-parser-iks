package com.dp_ua.iksparser.bot.command.impl.competition;

import com.dp_ua.iksparser.bot.Icon;
import com.dp_ua.iksparser.bot.abilities.competition.CompetitionFacade;
import com.dp_ua.iksparser.bot.command.BaseCommand;
import com.dp_ua.iksparser.bot.message.Message;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.dp_ua.iksparser.bot.command.CommandArgumentName.COMPETITION_ID;


@Component
@ToString
public class CommandCompetitionNotLoaded extends BaseCommand {
    private final static String command = "competitionnotloaded";
    private final boolean isInTextCommand = false;
    @Autowired
    private CompetitionFacade competitionFacade;

    @Override
    public String command() {
        return command;
    }

    @Override
    protected String getTextForCallBackAnswer(Message message) {
        return Icon.INFO + " Дивимось інформацію по змаганню " + Icon.INFO;
    }

    @Override
    protected void perform(Message message) {
        String chatId = message.getChatId();
        String text = message.getMessageText();
        long competitionId = Long.parseLong(parseArgumentFromFullText(text, COMPETITION_ID));
        competitionFacade.showNotLoadedInfo(chatId, competitionId, message.getEditMessageId());
    }

    public static String getCallBackCommand(long competitionId) {
        return "/" + command + " {\"" + COMPETITION_ID.getValue() + "\":\"" + competitionId + "\"}";
    }
}

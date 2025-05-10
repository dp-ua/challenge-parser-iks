package com.dp_ua.iksparser.bot.command.impl.participants;

import static com.dp_ua.iksparser.bot.Icon.ATHLETE;

import org.springframework.stereotype.Component;

import com.dp_ua.iksparser.bot.abilities.action.ActionType;
import com.dp_ua.iksparser.bot.abilities.competition.CompetitionFacade;
import com.dp_ua.iksparser.bot.command.BaseCommand;
import com.dp_ua.iksparser.bot.command.CommandArgumentName;
import com.dp_ua.iksparser.bot.message.Message;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Component
@ToString
@RequiredArgsConstructor
public class CommandShowHeatLinesInCompetitionForParticipant extends BaseCommand {
    private static final String COMMAND = "shlcfp"; // show heat lines in competition for participant
    private final CompetitionFacade competitionFacade;

    @Override
    public String command() {
        return COMMAND;
    }

    @Override
    protected String getTextForCallBackAnswer(Message message) {
        return ATHLETE + " подробиці участі спортсмена в змаганнях";
    }

    @Override
    protected void perform(Message message) {
        String chatId = message.getChatId();
        Integer editMessageId = message.getEditMessageId();
        String text = message.getMessageText();
        long participantId = Long.parseLong(parseArgumentFromFullText(text, CommandArgumentName.PARTICIPANT_ID));
        long competitionId = Long.parseLong(parseArgumentFromFullText(text, CommandArgumentName.COMPETITION_ID));

        competitionFacade.showAthleteCompetitionParticipationDetails(chatId, editMessageId, participantId, competitionId);
    }

    public static String getCallbackCommand(long participantId, long competitionId) {
        return SLASH + COMMAND + BRACKET_OPEN
                + paramParticipant(participantId)
                + PARAM_DELIMITER
                + paramCompetition(competitionId)
                + BRACKET_CLOSE;
    }

    public static String getCallbackCommand(long participantId, long competitionId, ActionType action) {
        return SLASH + COMMAND + BRACKET_OPEN
                + paramParticipant(participantId)
                + PARAM_DELIMITER
                + paramCompetition(competitionId)
                + PARAM_DELIMITER
                + paramAction(action)
                + BRACKET_CLOSE;
    }
}
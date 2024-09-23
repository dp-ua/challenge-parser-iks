package com.dp_ua.iksparser.bot.command.impl.participants;

import com.dp_ua.iksparser.bot.Icon;
import com.dp_ua.iksparser.bot.command.BaseCommand;
import com.dp_ua.iksparser.bot.message.Message;
import lombok.ToString;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;

import static com.dp_ua.iksparser.bot.command.CommandArgumentName.COMPETITION_ID;
import static com.dp_ua.iksparser.bot.command.CommandArgumentName.PARTICIPANT_ID;

@Component
@ToString
public class CommandParticipantCompetitionDetails extends BaseCommand {
    private final static String command = "parcompdet"; // show participant competition details

    @Override
    protected String getTextForCallBackAnswer(Message message) {
        return Icon.ATHLETE + " " + Icon.COMPETITION;
    }

    @Override
    protected void perform(Message message) {
        throw new NotImplementedException("Not implemented yet");
    }

    @Override
    public String command() {
        return command;
    }

    public static String getCallbackCommand(long participantId, long competitionId) {
        return "/" + command + " {\"" + PARTICIPANT_ID.getValue() + "\":\"" + participantId + "\",\"" + COMPETITION_ID.getValue() + "\":\"" + competitionId + "\"}";
    }
}

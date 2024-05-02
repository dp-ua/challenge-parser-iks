package com.dp_ua.iksparser.bot.command.impl.admin;

import com.dp_ua.iksparser.bot.abilities.participant.ParticipantFacade;
import com.dp_ua.iksparser.bot.command.AdminAccessCommand;
import com.dp_ua.iksparser.bot.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommandDuplicates extends AdminAccessCommand {
    public static final String command = "duplicates";
    @Autowired
    ParticipantFacade participantFacade;

    @Override
    protected String getTextForCallBackAnswer(Message message) {
        return "";
    }

    @Override
    protected void perform(Message message) {
        participantFacade.operateDuplicates();
    }

    @Override
    public String command() {
        return command;
    }
}

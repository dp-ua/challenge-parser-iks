package com.dp_ua.iksparser.bot.command.impl.admin;

import com.dp_ua.iksparser.bot.command.AdminAccessCommand;
import com.dp_ua.iksparser.bot.message.Message;
import com.dp_ua.iksparser.service.db.DbServiceOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommandDuplicates extends AdminAccessCommand {
    public static final String command = "duplicates";
    @Autowired
    DbServiceOperation dbServiceOperation;

    @Override
    protected String getTextForCallBackAnswer(Message message) {
        return "";
    }

    @Override
    protected void perform(Message message) {
        dbServiceOperation.operateDuplicates();
    }

    @Override
    public String command() {
        return command;
    }
}

package com.dp_ua.iksparser.bot.command;

import com.dp_ua.iksparser.bot.message.Message;

public class TestCommand extends BaseCommand {
    @Override
    public String command() {
        return "test";
    }

    @Override
    protected String getTextForCallBackAnswer(Message message) {
        return "";
    }

    @Override
    protected void perform(Message message) {
        // do nothing
    }

    @Override
    public void execute(Message message) {
        // do nothing
    }
}

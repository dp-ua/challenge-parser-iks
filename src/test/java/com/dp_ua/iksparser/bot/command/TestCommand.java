package com.dp_ua.iksparser.bot.command;

import com.dp_ua.iksparser.bot.message.Message;

public class TestCommand implements CommandInterface {
    @Override
    public String command() {
        return "test";
    }

    @Override
    public void execute(Message message) {
        // do nothing
    }
}

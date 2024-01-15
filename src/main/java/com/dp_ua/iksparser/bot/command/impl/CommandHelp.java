package com.dp_ua.iksparser.bot.command.impl;

import com.dp_ua.iksparser.bot.command.CommandInterface;
import com.dp_ua.iksparser.bot.message.Message;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.stereotype.Component;


@Component
@ToString
@EqualsAndHashCode
public class CommandHelp implements CommandInterface {
    private final String command = "help";
    private final boolean isInTextCommand = false;

    @Override
    public String command() {
        return command;
    }

    @Override
    public void execute(Message message) {
        throw new RuntimeException("Not implemented yet");
    }
}

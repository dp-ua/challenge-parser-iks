package com.dp_ua.iksparser.bot.command.impl;

import com.dp_ua.iksparser.bot.command.CommandInterface;
import com.dp_ua.iksparser.bot.command.CommandProvider;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@ToString
@EqualsAndHashCode
public class CommandHelp implements CommandInterface {
    private final String command = "help";
    private final boolean isInTextCommand = false;
    @Autowired
    CommandProvider commandProvider;

    @Override
    public String command() {
        return command;
    }
}

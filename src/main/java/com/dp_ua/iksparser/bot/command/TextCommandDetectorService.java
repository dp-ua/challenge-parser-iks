package com.dp_ua.iksparser.bot.command;

import com.dp_ua.iksparser.exeption.NotForMeException;

import java.util.List;

public interface TextCommandDetectorService {
    List<CommandInterface> getParsedCommands(String text) throws NotForMeException;
}

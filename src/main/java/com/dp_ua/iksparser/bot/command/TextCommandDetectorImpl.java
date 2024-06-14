package com.dp_ua.iksparser.bot.command;

import com.dp_ua.iksparser.bot.Bot;
import com.dp_ua.iksparser.exeption.NotForMeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TextCommandDetectorImpl implements TextCommandDetectorService {
    public static final String CLASSIC_COMMAND_FIRST_SYMBOL = "/";
    public static final String AT = "@";
    @Autowired
    Bot bot;
    @Autowired
    CommandProvider commandProvider;

    private String getBotName() {
        return bot.getBotUsername();
    }

    @Override
    public List<CommandInterface> getParsedCommands(String text) throws NotForMeException {
        log.debug("getParsedCommands: {}", text);
        if (isCanBeCommand(text)) {
            List<CommandInterface> simpleCommands = getParsedSimpleCommands(text);
            if (simpleCommands != null) return simpleCommands;
            // parse full string commands TODO
            // parse part of string commands TODO
        }
        return new ArrayList<>();
    }

    private boolean isCanBeCommand(String text) {
        return text != null || text.length() > 2;
    }

    private List<CommandInterface> getParsedSimpleCommands(String text) throws NotForMeException {
        if (text.startsWith(CLASSIC_COMMAND_FIRST_SYMBOL)) {
            String[] splitText = text.split("\\s+");
            boolean commandForBot = isCommandForBot(splitText[0]);
            if (!commandForBot) {
                throw new NotForMeException("Command not for me");
            }
            String txtForParse = cutCommand(splitText[0]);

            return commandProvider.getCommands().stream()
                    .filter(command -> command.allSimpleCommands().contains(txtForParse))
                    .collect(java.util.stream.Collectors.toList());
        }
        return null;
    }

    private String cutCommand(String s) {
        return (s.contains(AT) ? s.substring(1, s.indexOf(AT)) : s.substring(1))
                .toLowerCase();
    }

    private boolean isCommandForBot(String s) {
        String text = s.substring(1);

        if (text.endsWith(AT + getBotName())) {
            return true;
        } else return !text.contains(AT);
    }
}

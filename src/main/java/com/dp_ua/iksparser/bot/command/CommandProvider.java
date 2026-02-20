package com.dp_ua.iksparser.bot.command;

import java.util.List;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import com.dp_ua.iksparser.exeption.DuplicateCommandException;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Component
public class CommandProvider {

    private final List<CommandInterface> commands;

    public CommandProvider(List<CommandInterface> commands) {
        this.commands = commands;
        init();
    }

    private void init() {
        log.info("Initializing CommandProvider");
        commands.forEach(this::preRegisterCheck);
    }

    public List<BotCommand> menuCommands() {
        return commands.stream()
                .filter(CommandInterface::isNeedToAddToMenu)
                .map(c -> new BotCommand(c.command(), c.description()))
                .toList();
    }

    private void preRegisterCheck(CommandInterface command) {
        log.info("Registering command: {}", command.logString());
        checkCommand(command);
    }

    private void checkCommand(CommandInterface command) {
        checkSimpleCommands(command);
        checkPartOfStringCommands(command);
        command.fullStringCommands().forEach(cmd -> commands.forEach(c -> {
            if (c != command && c.fullStringCommands().contains(cmd)) {
                throw new DuplicateCommandException("Command already exists [" + cmd + "] in "
                        + c.getClass().getSimpleName() + " and "
                        + command.getClass().getSimpleName());
            }

        }));
    }

    private void checkPartOfStringCommands(CommandInterface command) {
        command.partOfStringCommands().forEach(partOfStringCmd -> {
            command.fullStringCommands().forEach(fullCmd -> {
                if (fullCmd.contains(partOfStringCmd)) {
                    throw new DuplicateCommandException("FullString command contain partCommand [" + partOfStringCmd + "] in "
                            + command.getClass().getSimpleName());
                }
            });
            commands.forEach(c -> {
                if (c != command && c.partOfStringCommands().contains(partOfStringCmd)) {
                    throw new DuplicateCommandException("Command already exists [" + partOfStringCmd + "] in "
                            + c.getClass().getSimpleName() + " and "
                            + command.getClass().getSimpleName());
                }
                c.fullStringCommands().forEach(fullCmd -> {
                    if (fullCmd.contains(partOfStringCmd)) {
                        throw new DuplicateCommandException("FullString command contain partCommand [" + partOfStringCmd + "] in "
                                + c.getClass().getSimpleName() + " and "
                                + command.getClass().getSimpleName());
                    }
                });
            });
        });
    }

    private void checkSimpleCommands(CommandInterface command) {
        command.allSimpleCommands().forEach(cmd -> commands.forEach(c -> {
            if (c != command && c.allSimpleCommands().contains(cmd)) {
                throw new DuplicateCommandException("Command already exists [" + cmd + "] in "
                        + c.getClass().getSimpleName() + " and "
                        + command.getClass().getSimpleName());
            }
        }));
    }

}

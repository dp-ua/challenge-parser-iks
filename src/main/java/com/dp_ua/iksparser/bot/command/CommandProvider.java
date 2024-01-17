package com.dp_ua.iksparser.bot.command;

import com.dp_ua.iksparser.SpringApp;
import com.dp_ua.iksparser.exeption.DuplicateCommandException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CommandProvider implements ApplicationListener<ContextRefreshedEvent>, Ordered {
    @Override
    public int getOrder() {
        return SpringApp.ORDER_FOR_COMMAND_PROVIDER;
    }
    @Autowired
    ApplicationContext context;

    @Getter
    private List<CommandInterface> commands = new ArrayList<>();

    @Override
    public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
        init();
    }

    private void init() {
        log.info("Initializing CommandProvider");
        commands = context.getBeansOfType(CommandInterface.class).values().stream()
                .peek(this::preRegisterCheck)
                .collect(Collectors.toList());
    }

    private void preRegisterCheck(CommandInterface command) {
        log.info("Registering command: {}", command.logString());
        checkCommand(command);
    }

    private void checkCommand(CommandInterface command) {
        checkSimpleCommands(command);
        checkPartOfStringCommands(command);
        command.fullStringCommands().forEach(cmd -> commands.forEach(c -> {
            if (c.fullStringCommands().contains(cmd)) {
                throw new DuplicateCommandException("Command already exists [" + cmd + "] in "
                        + c.getClass().getSimpleName() + " and "
                        + command.getClass().getSimpleName());
            }
        }));
    }

    // TODO test this method
    private void checkPartOfStringCommands(CommandInterface command) {
        command.partOfStringCommands().forEach(partOfStringCmd -> {
            command.fullStringCommands().forEach(fullCmd -> {
                if (fullCmd.contains(partOfStringCmd)) {
                    throw new DuplicateCommandException("FullString command contain partCommand [" + partOfStringCmd + "] in "
                            + command.getClass().getSimpleName());
                }
            });
            commands.forEach(c -> {
                if (c.partOfStringCommands().contains(partOfStringCmd)) {
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
            if (c.allSimpleCommands().contains(cmd)) {
                throw new DuplicateCommandException("Command already exists [" + cmd + "] in "
                        + c.getClass().getSimpleName() + " and "
                        + command.getClass().getSimpleName());
            }
        }));
    }
}

package com.dp_ua.iksparser.bot.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import com.dp_ua.iksparser.exeption.DuplicateCommandException;

class CommandProviderTest {

    @Test
    void shouldReturnAllCommandsWithoutOrder() {
        var cmd1 = mock(CommandInterface.class);
        when(cmd1.allSimpleCommands()).thenReturn(List.of("a"));
        when(cmd1.fullStringCommands()).thenReturn(List.of("a"));
        when(cmd1.partOfStringCommands()).thenReturn(List.of());
        when(cmd1.isNeedToAddToMenu()).thenReturn(true);
        when(cmd1.command()).thenReturn("a");
        when(cmd1.description()).thenReturn("desc");

        var cmd2 = mock(CommandInterface.class);
        when(cmd2.allSimpleCommands()).thenReturn(List.of("b"));
        when(cmd2.fullStringCommands()).thenReturn(List.of("b"));
        when(cmd2.partOfStringCommands()).thenReturn(List.of());
        when(cmd2.isNeedToAddToMenu()).thenReturn(false);
        when(cmd2.command()).thenReturn("b");
        when(cmd2.description()).thenReturn("desc");

        var provider = new CommandProvider(List.of(cmd1, cmd2));

        var allCommands = provider.getCommands().stream()
                .flatMap(c -> c.allSimpleCommands().stream())
                .toList();

        assertThat(allCommands).containsExactlyInAnyOrder("a", "b");
    }

    @Test
    void shouldReturnOnlyMenuCommands() {
        var menuCmd = mock(CommandInterface.class);
        when(menuCmd.isNeedToAddToMenu()).thenReturn(true);
        when(menuCmd.command()).thenReturn("menu");
        when(menuCmd.description()).thenReturn("Меню");

        var hiddenCmd = mock(CommandInterface.class);
        when(hiddenCmd.isNeedToAddToMenu()).thenReturn(false);

        var provider = new CommandProvider(List.of(menuCmd, hiddenCmd));

        List<BotCommand> menu = provider.menuCommands();
        assertThat(menu).hasSize(1);
        assertThat(menu.get(0).getCommand()).isEqualTo("menu");
        assertThat(menu.get(0).getDescription()).isEqualTo("Меню");
    }

    @Test
    void shouldThrowOnDuplicateSimpleCommand() {
        var cmd1 = mock(CommandInterface.class);
        when(cmd1.allSimpleCommands()).thenReturn(List.of("dup"));
        when(cmd1.fullStringCommands()).thenReturn(List.of());
        when(cmd1.partOfStringCommands()).thenReturn(List.of());

        var cmd2 = mock(CommandInterface.class);
        when(cmd2.allSimpleCommands()).thenReturn(List.of("dup"));
        when(cmd2.fullStringCommands()).thenReturn(List.of());
        when(cmd2.partOfStringCommands()).thenReturn(List.of());

        var commands = List.of(cmd1, cmd2);

        assertThrows(DuplicateCommandException.class, () -> new CommandProvider(commands));
    }

    @Test
    void shouldThrowOnDuplicateFullStringCommand() {
        var cmd1 = mock(CommandInterface.class);
        when(cmd1.allSimpleCommands()).thenReturn(List.of());
        when(cmd1.fullStringCommands()).thenReturn(List.of("full"));
        when(cmd1.partOfStringCommands()).thenReturn(List.of());

        var cmd2 = mock(CommandInterface.class);
        when(cmd2.allSimpleCommands()).thenReturn(List.of());
        when(cmd2.fullStringCommands()).thenReturn
                (List.of("full"));
        when(cmd2.partOfStringCommands()).thenReturn(List.of());

        var commands = List.of(cmd1, cmd2);

        assertThrows(DuplicateCommandException.class, () -> new CommandProvider(commands));
    }

}

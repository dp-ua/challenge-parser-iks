package com.dp_ua.iksparser.bot.command.impl.competition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandCompetitionsTest {
    CommandCompetitions command;

    @BeforeEach
    void setUp() {
        command = new CommandCompetitions();
    }

    @Test
    void shouldReturnCorrectCallbackCommand() {
        String expected = "/competitions {\"pg\":\"1\"}";
        String actual = CommandCompetitions.getCallbackCommand(1);
        assertEquals(expected, actual);
    }
}
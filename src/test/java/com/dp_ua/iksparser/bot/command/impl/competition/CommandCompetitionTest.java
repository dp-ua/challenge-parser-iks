package com.dp_ua.iksparser.bot.command.impl.competition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandCompetitionTest {
    CommandCompetition command;
    @BeforeEach
    public void setUp() {
        command = new CommandCompetition();
    }

    @Test
    void shouldReturnCorrectCallbackCommand() {
        String expected = "/competition {\"cid\":\"1\"}";
        String actual = command.getCallbackCommand(1);
        assertEquals(expected, actual);
    }
}
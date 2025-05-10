package com.dp_ua.iksparser.bot.command.impl.competition;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CommandCompetitionTest {
    @Test
    void shouldReturnCorrectCallbackCommand() {
        String expected = "/competition {\"ci\":\"1\"}";
        String actual = CommandCompetition.getCallbackCommand(1);
        assertEquals(expected, actual);
    }
}
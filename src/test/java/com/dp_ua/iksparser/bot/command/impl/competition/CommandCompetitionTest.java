package com.dp_ua.iksparser.bot.command.impl.competition;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandCompetitionTest {
    @Test
    void shouldReturnCorrectCallbackCommand() {
        String expected = "/competition {\"cid\":\"1\"}";
        String actual = CommandCompetition.getCallbackCommand(1);
        assertEquals(expected, actual);
    }
}
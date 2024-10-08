package com.dp_ua.iksparser.bot.command.impl.participants;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandShowFindAllParticipantsTest {
    CommandShowFindAllParticipants command;

    @BeforeEach
    public void setUp() {
        command = new CommandShowFindAllParticipants();
    }

    @Test
    void shouldGenerateCallBackCommand() {
        // given
        int page = 1;
        String search = "search";
        String expected = "/salps {\"pg\":\"1\",\"sh\":\"search\"}";
        // when
        String actual = CommandShowFindAllParticipants.getCallbackCommand(page, search);
        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldGenerateStateText() {
        // given
        int page = 1;
        String expected = "/salps {\"pg\":\"1\",\"sh\":\"{}\"}";
        // when
        String actual = CommandShowFindAllParticipants.getStateText(page);
        // then
        assertEquals(expected, actual);
    }
}
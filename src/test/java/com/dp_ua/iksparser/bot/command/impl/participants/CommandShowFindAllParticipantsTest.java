package com.dp_ua.iksparser.bot.command.impl.participants;

import org.junit.Before;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

class CommandShowFindAllParticipantsTest {
    CommandShowFindAllParticipants command;

    @Before
    public void setUp() {
        command = new CommandShowFindAllParticipants();
    }

    @Test
    void shouldGenerateCallBackCommand() {
        // given
        int page = 1;
        String search = "search";
        String expected = "/showallparticipants {\"pg\":\"1\",\"search\":\"search\"}";
        // when
        String actual = CommandShowFindAllParticipants.getCallbackCommand(page, search);
        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldGenerateStateText() {
        // given
        int page = 1;
        String expected = "/showallparticipants {\"pg\":\"1\",\"search\":\"{}\"}";
        // when
        String actual = CommandShowFindAllParticipants.getStateText(page);
        // then
        assertEquals(expected, actual);
    }
}
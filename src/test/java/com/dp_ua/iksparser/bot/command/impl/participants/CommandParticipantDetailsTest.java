package com.dp_ua.iksparser.bot.command.impl.participants;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dp_ua.iksparser.bot.abilities.action.ActionType;

class CommandParticipantDetailsTest {
    CommandParticipantDetails command;

    @BeforeEach
    void setUp() {
        command = new CommandParticipantDetails();
    }

    @Test
    void shouldGetCallBackTextWithoutAction() {
        // given
        int page = 1;
        long id = 2;
        String expected = "/shpartdet {\"pg\":\"1\",\"pi\":\"2\"}";
        // when
        String actual = CommandParticipantDetails.getCallbackCommand(page, id);
        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldGetCallBackTextWithAction() {
        // given
        int page = 1;
        long id = 2;
        String expected = "/shpartdet {\"pg\":\"1\",\"pi\":\"2\",\"a\":\"SUB\"}";
        // when
        String actual = CommandParticipantDetails.getCallbackCommand(page, id, ActionType.SUB);
        // then
        assertEquals(expected, actual);
    }
}
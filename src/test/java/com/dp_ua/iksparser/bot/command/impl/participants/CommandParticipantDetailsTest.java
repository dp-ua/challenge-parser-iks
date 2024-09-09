package com.dp_ua.iksparser.bot.command.impl.participants;

import com.dp_ua.iksparser.bot.abilities.action.ActionType;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandParticipantDetailsTest {
    CommandParticipantDetails command;

    @Before
    public void setUp() {
        command = new CommandParticipantDetails();
    }

    @Test
    void shouldGetCallBackTextWithoutAction() {
        // given
        int page = 1;
        long id = 2;
        String expected = "/shpartdet {\"pg\":\"1\",\"pid\":\"2\"}";
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
        String expected = "/shpartdet {\"pg\":\"1\",\"pid\":\"2\",\"act\":\"SUB\"}";
        // when
        String actual = CommandParticipantDetails.getCallbackCommand(page, id, ActionType.SUB);
        // then
        assertEquals(expected, actual);
    }
}
package com.dp_ua.iksparser.bot.command.impl.participants;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandParticipantCompetitionDetailsTest {

    @Test
    void shouldGenerateCorrectCallBackText() {
        // given
        long participantId = 1;
        long competitionId = 2;
        String expected = "/parcompdet {\"pid\":\"1\",\"cid\":\"2\"}";
        // when
        String actual = CommandParticipantCompetitionDetails.getCallbackCommand(participantId, competitionId);
        // then
        assertEquals(expected, actual);
    }
}
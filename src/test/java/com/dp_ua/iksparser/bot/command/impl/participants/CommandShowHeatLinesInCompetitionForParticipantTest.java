package com.dp_ua.iksparser.bot.command.impl.participants;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.dp_ua.iksparser.bot.abilities.action.ActionType.SUB;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandShowHeatLinesInCompetitionForParticipantTest {
    CommandShowHeatLinesInCompetitionForParticipant command;

    @BeforeEach
    void setUp() {
        command = new CommandShowHeatLinesInCompetitionForParticipant();
    }

    @Test
    void shouldGenerateCorrectCallbackText_withoutAction() {
        String expected = "/showhtlincomfpart {\"pid\":\"1\",\"cid\":\"2\"}";
        String actual = CommandShowHeatLinesInCompetitionForParticipant.getCallbackCommand(1, 2);
        assertEquals(expected, actual);
    }

    @Test
    void shouldGenerateCorrectCallbackText_withAction() {
        String expected = "/showhtlincomfpart {\"pid\":\"1\",\"cid\":\"2\",\"act\":\"SUB\"}";
        String actual = CommandShowHeatLinesInCompetitionForParticipant.getCallbackCommand(1, 2, SUB);
        assertEquals(expected, actual);
    }
}
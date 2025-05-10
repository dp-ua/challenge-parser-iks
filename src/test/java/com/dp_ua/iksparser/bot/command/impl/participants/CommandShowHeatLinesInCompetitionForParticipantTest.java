package com.dp_ua.iksparser.bot.command.impl.participants;

import static com.dp_ua.iksparser.bot.abilities.action.ActionType.SUB;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.dp_ua.iksparser.bot.abilities.competition.CompetitionFacade;

class CommandShowHeatLinesInCompetitionForParticipantTest {
    CommandShowHeatLinesInCompetitionForParticipant command;
    @Mock
    CompetitionFacade competitionFacade;

    @BeforeEach
    void setUp() {
        command = new CommandShowHeatLinesInCompetitionForParticipant(competitionFacade);
    }

    @Test
    void shouldGenerateCorrectCallbackText_withoutAction() {
        String expected = "/shlcfp {\"pi\":\"1\",\"ci\":\"2\"}";
        String actual = CommandShowHeatLinesInCompetitionForParticipant.getCallbackCommand(1, 2);
        assertEquals(expected, actual);
    }

    @Test
    void shouldGenerateCorrectCallbackText_withAction() {
        String expected = "/shlcfp {\"pi\":\"1\",\"ci\":\"2\",\"a\":\"SUB\"}";
        String actual = CommandShowHeatLinesInCompetitionForParticipant.getCallbackCommand(1, 2, SUB);
        assertEquals(expected, actual);
    }
}
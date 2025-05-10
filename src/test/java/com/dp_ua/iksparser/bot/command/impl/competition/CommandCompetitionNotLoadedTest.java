package com.dp_ua.iksparser.bot.command.impl.competition;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommandCompetitionNotLoadedTest {
    CommandCompetitionNotLoaded commandCompetitionNotLoaded;

    @BeforeEach
    void setUp() {
        commandCompetitionNotLoaded = new CommandCompetitionNotLoaded();
    }

    @Test
    void shouldGenerateCorrectCallBackText(){
        String expected = "/competitionnotloaded {\"ci\":\"1\"}";
        String actual = CommandCompetitionNotLoaded.getCallBackCommand(1);
        assertEquals(expected, actual);
    }
}
package com.dp_ua.iksparser.bot.command.impl.competition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandCompetitionNotLoadedTest {
    CommandCompetitionNotLoaded commandCompetitionNotLoaded;

    @BeforeEach
    void setUp() {
        commandCompetitionNotLoaded = new CommandCompetitionNotLoaded();
    }

    @Test
    void shouldGenerateCorrectCallBackText(){
        String expected = "/competitionnotloaded {\"cid\":\"1\"}";
        String actual = CommandCompetitionNotLoaded.getCallBackCommand(1);
        assertEquals(expected, actual);
    }
}
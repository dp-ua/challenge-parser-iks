package com.dp_ua.iksparser.bot.command.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandSearchByBibNumberTest {
CommandSearchByBibNumber command;
    @BeforeEach
    void setUp() {
        command = new CommandSearchByBibNumber();
    }

    @Test
    void shouldGenerateCorrectCallBackCommand() {
        String expected = "/searchbybibnumber {\"cid\":\"123\"}";
        String actual = CommandSearchByBibNumber.getCallbackCommand(123);
        assertEquals(expected, actual);
    }
}
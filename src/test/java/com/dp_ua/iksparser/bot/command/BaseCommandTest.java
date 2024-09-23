package com.dp_ua.iksparser.bot.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BaseCommandTest {
    BaseCommand command;

    @BeforeEach
    public void setUp() {
        command = new TestCommand();
    }

    @Test
    public void testGetCommandArgumentString_withArguments() {
        BaseCommand command = new TestCommand();
        String result = command.getCommandArgumentString("/test arg1 arg2");
        assertEquals("arg1 arg2", result);
    }

    @Test
    public void testGetCommandArgumentString_withArguments_EndLineInsteadOfSpace() {
        BaseCommand command = new TestCommand();
        String result = command.getCommandArgumentString("/test\narg1 arg2");
        assertEquals("arg1 arg2", result);
    }

    @Test
    public void testGetCommandArgumentString_withBotName() {
        BaseCommand command = new TestCommand();
        String result = command.getCommandArgumentString("/test@Bot_name arg1 arg2");
        assertEquals("arg1 arg2", result);
    }

    @Test
    public void testGetCommandArgumentString_noArguments() {
        BaseCommand command = new TestCommand();
        String result = command.getCommandArgumentString("/test");
        assertEquals("", result);
    }

    @Test
    public void testGetCommandArgumentString_withBotName_noArguments() {
        BaseCommand command = new TestCommand();
        String result = command.getCommandArgumentString("/test@Bot_name");
        assertEquals("", result);
    }

    @Test
    public void testGetCommandArgumentString_invalidCommand() {
        BaseCommand command = new TestCommand();
        String result = command.getCommandArgumentString("/othercommand arg1");
        assertEquals("", result);
    }
}
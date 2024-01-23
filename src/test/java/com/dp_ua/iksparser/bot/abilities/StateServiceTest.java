package com.dp_ua.iksparser.bot.abilities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StateServiceTest {
    StateService stateService;

    @BeforeEach
    void setUp() {
        stateService = new StateService();
    }

    @Test
    void shouldSetAndGetState() {
        stateService.setState("1", "2");
        assert stateService.getState("1").equals("2");
    }

    @Test
    void shouldFormatString() {
        String template = "Hello, {}! You are {} years old.";
        String[] args = {"John", "25"};
        String result = StateService.formatArgs(template, args);
        assert result.equals("Hello, John! You are 25 years old.");
    }
}
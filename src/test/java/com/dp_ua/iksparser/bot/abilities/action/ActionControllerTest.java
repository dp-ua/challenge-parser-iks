package com.dp_ua.iksparser.bot.abilities.action;

import com.dp_ua.iksparser.service.JsonReader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest(classes = {ActionController.class, JsonReader.class})
class ActionControllerTest {
    @Autowired
    ActionController actionController;

    @Test
    void getActionType_shouldBeEmpty() {
        Optional<ActionType> subscribe = actionController.getActionType("subscribe");
        assert subscribe.isEmpty();
    }
    // todo MORE tests
}
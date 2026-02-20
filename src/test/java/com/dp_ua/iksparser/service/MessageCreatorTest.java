package com.dp_ua.iksparser.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MessageCreatorTest {

    MessageCreator service;

    @BeforeEach
    void setUp() {
        service = MessageCreator.SERVICE;
    }

    @Test
    void shouldRemoveDuplicates() {
        List<String> inputList = new ArrayList<>();
        inputList.add("apple");
        inputList.add("banana");
        inputList.add("apple");
        inputList.add("orange");
        inputList.add("banana");

        List<String> expectedList = new ArrayList<>();
        expectedList.add("apple");
        expectedList.add("banana");
        expectedList.add("orange");

        List<String> actualList = service.removeDuplicates(inputList);
        assertEquals(expectedList, actualList);
    }

}

package com.dp_ua.iksparser.service;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MessageCreatorTest {
    MessageCreator service;

    @Before
    public void setUp() {
        service = MessageCreator.SERVICE;
    }


    @Test
    public void shouldRemoveDuplicates() {
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
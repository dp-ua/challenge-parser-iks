package com.dp_ua.iksparser.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dp_ua.iksparser.dba.entity.DayEntity;
import com.dp_ua.iksparser.service.parser.ServiceParser;

class ServiceParserTest {

    static final String INPUT = "15.12.23 (День 1 | Day 1) (151223)";
    static final String INPUT_WRONG = "15.12.23 (День 1 | Day 1) (151223) (161223)";

    ServiceParser parser;

    @BeforeEach
    void setUp() {
        parser = new ServiceParser();
    }

    @Test
    void testToString() {
        String result = parser.parseDay(INPUT).toString();
        assertEquals("DayEntity{date='15.12.23', dateId='151223', dayName='День 1', dayNameEn='Day 1'}", result);
    }

    @Test
    void testParse() {
        DayEntity day = parser.parseDay(INPUT);
        assertEquals("15.12.23", day.getDate());
        assertEquals("151223", day.getDateId());
        assertEquals("День 1", day.getDayName());
        assertEquals("Day 1", day.getDayNameEn());
    }

    @Test
    void testParseWrong() {
        assertThrows(IllegalArgumentException.class, () -> parser.parseDay(INPUT_WRONG));
    }

}

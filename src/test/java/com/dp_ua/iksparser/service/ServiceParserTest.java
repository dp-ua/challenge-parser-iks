package com.dp_ua.iksparser.service;

import com.dp_ua.iksparser.element.DayEntity;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ServiceParserTest {
    public static String INPUT = "15.12.23 (День 1 | Day 1) (151223)";
    public static String INPUT_WRONG = "15.12.23 (День 1 | Day 1) (151223) (161223)";

    ServiceParser parser;

    @Before
    public void setUp() {
        parser = new ServiceParser();
    }

    @Test
    public void testToString() {
        String result = parser.parseDay(INPUT).toString();
        assertEquals("Day(date=15.12.23, dateId=151223, dayName=День 1, dayNameEn=Day 1, events=[])", result);
    }

    @Test
    public void testParse() {
        DayEntity day = parser.parseDay(INPUT);
        assertEquals("15.12.23", day.getDate());
        assertEquals("151223", day.getDateId());
        assertEquals("День 1", day.getDayName());
        assertEquals("Day 1", day.getDayNameEn());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseWrong() {
        parser.parseDay(INPUT_WRONG);
    }

}
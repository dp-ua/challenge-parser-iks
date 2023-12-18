package com.example;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DayTest {
    public static String INPUT = "15.12.23 (День 1 | Day 1) (151223)";
    public static String INPUT_WRONG = "15.12.23 (День 1 | Day 1) (151223) (151223)";

    @Test
    public void testToString() {
        String result = Day.parse(INPUT).toString();
        assertEquals("Day(date=15.12.23, dateId=151223, dayName=День 1, dayNameEn=Day 1)", result);
    }

    @Test
    public void testParse() {
        Day day = Day.parse(INPUT);
        assertEquals("15.12.23", day.getDate());
        assertEquals("151223", day.getDateId());
        assertEquals("День 1", day.getDayName());
        assertEquals("Day 1", day.getDayNameEn());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseWrong() {
        Day.parse(INPUT_WRONG);
    }

}
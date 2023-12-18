package com.example.elements;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.java.Log;

@ToString
@Getter
@Log
public class Day {
    private final String date;
    private final String dateId;
    private final String dayName;
    private final String dayNameEn;

    public Day(String date, String dateId, String dayName, String dayNameEn) {
        this.date = date;
        this.dateId = dateId;
        this.dayName = dayName;
        this.dayNameEn = dayNameEn;
    }
}

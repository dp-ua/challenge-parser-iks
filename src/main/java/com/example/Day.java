package com.example;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.java.Log;

import static java.util.logging.Level.INFO;

@ToString
@Getter
@Log
public class Day {
    private String date;
    private String dateId;
    private String dayName;
    private String dayNameEn;

    public static Day parse(String input) {
        log.log(INFO, "parse: " + input);

        Day day = new Day();
        String[] parts = input.split(" ");
        checkParts(parts);
        day.date = parts[0];
        day.dateId = parts[6].substring(1, parts[6].length() - 1);
        day.dayName = parts[1].substring(1) + " " + parts[2];
        day.dayNameEn = parts[4] + " " + parts[5].substring(0, parts[5].length() - 1);
        return day;
    }

    private static void checkParts(String[] parts) {
        if (parts.length != 7) {
            throw new IllegalArgumentException("Wrong input string");
        }
        if (!parts[3].equals("|")) {
            throw new IllegalArgumentException("Wrong input string");
        }
    }
}

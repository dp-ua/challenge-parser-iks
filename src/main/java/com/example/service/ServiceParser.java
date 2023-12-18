package com.example.service;

import com.example.elements.Day;
import lombok.extern.java.Log;

import static java.util.logging.Level.INFO;

@Log
public class ServiceParser {
    public Day parseDay(String input) {
        log.log(INFO, "parse: " + input);
        String[] parts = input.split(" ");
        checkParts(parts);
        String date = parts[0];
        String dateId = parts[6].substring(1, parts[6].length() - 1);
        String dayName = parts[1].substring(1) + " " + parts[2];
        String dayNameEn = parts[4] + " " + parts[5].substring(0, parts[5].length() - 1);

        return new Day(date, dateId, dayName, dayNameEn);
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

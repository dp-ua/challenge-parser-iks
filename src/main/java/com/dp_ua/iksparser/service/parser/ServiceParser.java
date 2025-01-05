package com.dp_ua.iksparser.service.parser;

import com.dp_ua.iksparser.dba.entity.DayEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ServiceParser {
    public DayEntity parseDay(String input) {
        log.debug("Parsing day: " + input);
        String[] parts = input.split(" ");
        checkParts(parts);
        String date = parts[0];
        String dateId = parts[6].substring(1, parts[6].length() - 1);
        String dayName = parts[1].substring(1) + " " + parts[2];
        String dayNameEn = parts[4] + " " + parts[5].substring(0, parts[5].length() - 1);

        return new DayEntity(date, dateId, dayName, dayNameEn);
    }

    private void checkParts(String[] parts) {
        if (parts.length != 7) {
            throw new IllegalArgumentException("Wrong input string");
        }
        if (!parts[3].equals("|")) {
            throw new IllegalArgumentException("Wrong input string");
        }
    }

    public String cleanTextFromEmoji(String input) {
        return input.replaceAll("[\\p{So}\\p{Cn}]", "").trim();
    }
}

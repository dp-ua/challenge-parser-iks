package com.example.elements;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;

@ToString
@Getter
@Log
@EqualsAndHashCode
public class Day {
    private final String date;
    private final String dateId;
    private final String dayName;
    private final String dayNameEn;
    private final List<Event> events;

    public Day(String date, String dateId, String dayName, String dayNameEn) {
        this.date = date;
        this.dateId = dateId;
        this.dayName = dayName;
        this.dayNameEn = dayNameEn;
        events = new ArrayList<>();
    }

    public void addEvent(Event event) {
        events.add(event);
    }

    public List<Match> findMatchesBySurname(String surname) {
        List<Match> matches = new ArrayList<>();
        String strToMatch = surname.toLowerCase();

        for (Event event : events) {
            for (Heat heat : event.getHeats()) {
                for (Participant participant : heat.getParticipants()) {
                    if (participant.getSurname().toLowerCase().equals(strToMatch)) {
                        // Найдено совпадение - сохраняем информацию о нем
                        matches.add(new Match(this, event, heat, participant));
                    }
                }
            }
        }
        return matches;
    }
}

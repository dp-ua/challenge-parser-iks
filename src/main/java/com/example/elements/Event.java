package com.example.elements;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
@Getter
@EqualsAndHashCode
public class Event {
    private final String time;
    private final String eventName;
    private final String category;
    private final String round;
    private final String startListUrl;
    private List<Heat> heats;

    public Event(String time, String eventName, String category, String round, String startListUrl) {
        this.time = time;
        this.eventName = eventName;
        this.category = category;
        this.round = round;
        this.startListUrl = startListUrl;
        heats = new ArrayList<>();
    }

    public void addHeat(Heat heat) {
        heats.add(heat);
    }
}

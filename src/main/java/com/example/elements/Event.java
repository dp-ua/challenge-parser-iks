package com.example.elements;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class Event {
    private final String time;
    private final String eventName;
    private final String category;
    private final String round;
    private final String startListUrl;

    public Event(String time, String eventName, String category, String round, String startListUrl) {
        this.time = time;
        this.eventName = eventName;
        this.category = category;
        this.round = round;
        this.startListUrl = startListUrl;
    }
}

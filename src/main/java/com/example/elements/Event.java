package com.example.elements;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class Event {
    private String time;
    private String eventName;
    private String category;
    private String round;
    private String startListUrl;

    public Event(String time, String eventName, String category, String round, String startListUrl) {
        this.time = time;
        this.eventName = eventName;
        this.category = category;
        this.round = round;
        this.startListUrl = startListUrl;
    }
}

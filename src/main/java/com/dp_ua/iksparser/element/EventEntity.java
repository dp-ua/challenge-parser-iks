package com.dp_ua.iksparser.element;

import jakarta.persistence.Entity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
@Getter
@Entity
@NoArgsConstructor
@EqualsAndHashCode
public class EventEntity extends DomainElement {
    private String time;
    private String eventName;
    private String category;
    private String round;
    private String startListUrl;

    private List<HeatEntity> heats;

    public EventEntity(String time, String eventName, String category, String round, String startListUrl) {
        this.time = time;
        this.eventName = eventName;
        this.category = category;
        this.round = round;
        this.startListUrl = startListUrl;
        heats = new ArrayList<>();
    }

    public void addHeat(HeatEntity heat) {
        heats.add(heat);
    }
}

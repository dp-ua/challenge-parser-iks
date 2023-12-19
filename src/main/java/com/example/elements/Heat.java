package com.example.elements;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@EqualsAndHashCode
public class Heat {
    private final String name;
    private List<Participant> participants;

    public Heat(String heatName) {
        this.name = heatName;
        participants = new ArrayList<>();
    }

    public void addParticipant(Participant participant) {
        participants.add(participant);
    }
}

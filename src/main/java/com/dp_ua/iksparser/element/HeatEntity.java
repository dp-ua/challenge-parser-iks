package com.dp_ua.iksparser.element;

import jakarta.persistence.Entity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@EqualsAndHashCode
@Entity
@NoArgsConstructor
public class HeatEntity extends DomainElement {
    private String name;
    private List<Participant> participants;

    public HeatEntity(String heatName) {
        this.name = heatName;
        participants = new ArrayList<>();
    }

    public void addParticipant(Participant participant) {
        participants.add(participant);
    }
}

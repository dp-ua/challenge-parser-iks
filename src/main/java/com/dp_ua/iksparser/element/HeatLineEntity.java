package com.dp_ua.iksparser.element;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class HeatLineEntity extends DomainElement {
    private String lane;
    private String bib;
    @ManyToOne
    @JoinColumn(name = "participant_id")
    private ParticipantEntity participant;
    @ManyToMany
    @JoinTable(
            name = "heatline_coach",
            joinColumns = @JoinColumn(name = "heatline_id"),
            inverseJoinColumns = @JoinColumn(name = "coach_id")
    )
    private List<CoachEntity> coaches;
    @ManyToOne
    @JoinColumn(name = "heat_id")
    private HeatEntity heat;

    public HeatLineEntity() {
        coaches = new ArrayList<>();
    }

    public void addCoach(CoachEntity coach) {
        coaches.add(coach);
    }
}

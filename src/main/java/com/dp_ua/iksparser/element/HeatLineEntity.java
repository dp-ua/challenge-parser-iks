package com.dp_ua.iksparser.element;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@EqualsAndHashCode
@Entity
@NoArgsConstructor
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
    @ManyToMany
    @JoinTable(
            name = "heatline_heat",
            joinColumns = @JoinColumn(name = "heatline_id"),
            inverseJoinColumns = @JoinColumn(name = "heat_id")
    )
    private List<HeatEntity> heats;
}

package com.dp_ua.iksparser.element;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
    private ParticipantEntity participant; // todo add heatline to participant
    @ManyToMany
    @JoinTable(
            name = "heatline_coach",
            joinColumns = @JoinColumn(name = "heatline_id"),
            inverseJoinColumns = @JoinColumn(name = "coach_id")
    )
    private List<CoachEntity> coaches;
}

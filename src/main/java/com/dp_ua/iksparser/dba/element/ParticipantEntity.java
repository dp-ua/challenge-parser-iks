package com.dp_ua.iksparser.dba.element;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class ParticipantEntity extends DomainElement {
    private String surname;
    private String name;
    private String team;
    private String city;
    private String born;
    private String url;
    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL)
    private List<HeatLineEntity> heatLines;

    public ParticipantEntity() {
        heatLines = new ArrayList<>();
    }

    public void addHeatLine(HeatLineEntity heatLine) {
        heatLines.add(heatLine);
    }
}

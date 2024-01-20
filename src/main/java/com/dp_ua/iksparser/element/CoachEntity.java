package com.dp_ua.iksparser.element;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class CoachEntity extends DomainElement {
    private String name;
    @ManyToMany(mappedBy = "coaches")
    private List<HeatLineEntity> heatLines;

    public CoachEntity() {
        heatLines = new ArrayList<>();
    }

    public void addHeatLine(HeatLineEntity heatLine) {
        heatLines.add(heatLine);
    }
}

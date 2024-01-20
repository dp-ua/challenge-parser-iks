package com.dp_ua.iksparser.element;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class HeatEntity extends DomainElement {
    private String name;
    @OneToMany(mappedBy = "heat", cascade = CascadeType.ALL)
    private List<HeatLineEntity> heatLines;
    @ManyToOne
    @JoinColumn(name = "event_id")
    private EventEntity event;

    public HeatEntity() {
        heatLines = new ArrayList<>();
    }

    public void addHeatLine(HeatLineEntity heatLine) {
        heatLines.add(heatLine);
    }
}

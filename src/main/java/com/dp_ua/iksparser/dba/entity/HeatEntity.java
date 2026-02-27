package com.dp_ua.iksparser.dba.entity;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

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

    @Override
    public String toString() {
        return "HeatEntity{" +
                "name='" + name + '\'' +
                '}';
    }

    public HeatEntity() {
        heatLines = new ArrayList<>();
    }

    public void addHeatLine(HeatLineEntity heatLine) {
        heatLines.add(heatLine);
    }

    public Integer extractHeatNumber() {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        var parts = name.split("\\s+");
        for (String part : parts) {
            try {
                return Integer.parseInt(part.trim());
            } catch (NumberFormatException ignored) {
                // NoOp ignore not numeric data
            }
        }
        return null;
    }

}

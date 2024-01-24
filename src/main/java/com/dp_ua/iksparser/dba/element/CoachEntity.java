package com.dp_ua.iksparser.dba.element;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoachEntity coach = (CoachEntity) o;
        return Objects.equals(name, coach.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "CoachEntity{" +
                "id=" + id +
                "name='" + name + '\'' +
                '}';
    }
}

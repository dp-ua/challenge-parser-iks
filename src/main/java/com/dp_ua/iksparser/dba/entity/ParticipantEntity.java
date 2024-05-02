package com.dp_ua.iksparser.dba.entity;

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
    private String region;
    private String born;
    private String url;
    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL)
    private List<HeatLineEntity> heatLines;

    public String getUrl() {
        return codeURL(url);
    }

    @Override
    public String toString() {
        return "ParticipantEntity{" +
                "id=" + id +
                "surname='" + surname + '\'' +
                ", name='" + name + '\'' +
                ", team='" + team + '\'' +
                ", region='" + region + '\'' +
                ", born='" + born + '\'' +
                ", url='" + url + '\'' +
                '}';
    }

    public ParticipantEntity() {
        heatLines = new ArrayList<>();
    }

    public void addHeatLine(HeatLineEntity heatLine) {
        heatLines.add(heatLine);
    }
}

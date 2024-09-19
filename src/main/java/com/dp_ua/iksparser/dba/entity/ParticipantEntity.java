package com.dp_ua.iksparser.dba.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParticipantEntity that = (ParticipantEntity) o;
        return Objects.equals(surname, that.surname) && Objects.equals(name, that.name) && Objects.equals(team, that.team) && Objects.equals(region, that.region) && Objects.equals(born, that.born) && Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(surname, name, team, region, born, url);
    }
}

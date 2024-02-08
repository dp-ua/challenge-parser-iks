package com.dp_ua.iksparser.dba.element;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
@Slf4j
@Entity
@Getter
@Setter
@Table(indexes = {
        @Index(name = "idx_name", columnList = "name"),
        @Index(name = "idx_NameDateUrl", columnList = "name,beginDate,url")
})
public class CompetitionEntity extends DomainElement {
    private String name;
    private String status;
    private String beginDate;
    private String endDate;
    private String country;
    private String city;
    private String url;
    @OneToMany(mappedBy = "competition", cascade = CascadeType.ALL)
    private List<DayEntity> days;

    public String getUrl() {
        return codeURL(url);
    }

    public CompetitionEntity() {
        days = new ArrayList<>();
    }

    public void fillCompetition(CompetitionEntity competition) {
        this.url = competition.getUrl();
        this.beginDate = competition.getBeginDate();
        this.endDate = competition.getEndDate();
        this.name = competition.getName();
        this.country = competition.getCountry();
        this.city = competition.getCity();
        this.status = competition.getStatus();
    }

    public void addDay(DayEntity day) {
        days.add(day);
    }

    public boolean isNeedToUpdate() {
        if (days.isEmpty()) {
            return true;
        }
        return days.stream().anyMatch(DayEntity::isNeedToUpdate);
    }
}

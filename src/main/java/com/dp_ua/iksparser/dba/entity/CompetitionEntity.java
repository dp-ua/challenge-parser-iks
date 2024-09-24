package com.dp_ua.iksparser.dba.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompetitionEntity that = (CompetitionEntity) o;
        return Objects.equals(getName(), that.getName()) && Objects.equals(getStatus(), that.getStatus()) && Objects.equals(getBeginDate(), that.getBeginDate()) && Objects.equals(getEndDate(), that.getEndDate()) && Objects.equals(getCountry(), that.getCountry()) && Objects.equals(getCity(), that.getCity()) && Objects.equals(getUrl(), that.getUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getStatus(), getBeginDate(), getEndDate(), getCountry(), getCity(), getUrl());
    }

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

    public boolean isFilled() {
        return !getDays().isEmpty();
    }

    public boolean isURLEmpty() {
        return getUrl() == null || getUrl().isEmpty();
    }

    public boolean isCanBeUpdated() {
        return !isURLEmpty() || !isUrlNotValid();
    }

    public boolean isUrlNotValid() {
        return getUrl().endsWith(".pdf");
    }
}

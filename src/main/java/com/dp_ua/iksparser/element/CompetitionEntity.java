package com.dp_ua.iksparser.element;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@ToString
@Entity
@Getter
@Setter
@Table(indexes = {
        @Index(name = "idx_name", columnList = "name"),
        @Index(name = "idx_NameDateUrl", columnList = "name,beginDate,url")
})
public class CompetitionEntity extends DomainElement {

    private String url;
    private String beginDate;
    private String endDate;
    private String name;
    private String country;
    private String city;

    @OneToMany(mappedBy = "competition", cascade = CascadeType.ALL)
    private List<DayEntity> days;


    public void fillCompetition(CompetitionEntity competition) {
        this.url = competition.getUrl();
        this.beginDate = competition.getBeginDate();
        this.endDate = competition.getEndDate();
        this.name = competition.getName();
        this.country = competition.getCountry();
        this.city = competition.getCity();
    }
}

package com.dp_ua.iksparser.element;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@ToString
@Entity
@Getter
@Setter
@Table(indexes = {
        @Index(name = "idx_name", columnList = "name"),
        @Index(name = "idx_NameDateUrl", columnList = "name,beginDate,url")
})
public class Competition {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String url;
    private String beginDate;
    private String endDate;
    private String name;
    private String country;
    private String city;

    @Column(updatable = false)
    private String created;

    private String updated;

    @PrePersist
    protected void onCreate() {
        created = LocalDateTime.now().toString();
        updated = LocalDateTime.now().toString();
    }

    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now().toString();
    }

    public LocalDateTime getUpdatedTime() {
        return LocalDateTime.parse(updated);
    }

    public void fillCompetition(Competition competition) {
        this.url = competition.getUrl();
        this.beginDate = competition.getBeginDate();
        this.endDate = competition.getEndDate();
        this.name = competition.getName();
        this.country = competition.getCountry();
        this.city = competition.getCity();
    }
}

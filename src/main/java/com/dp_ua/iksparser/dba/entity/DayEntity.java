package com.dp_ua.iksparser.dba.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Slf4j
@Entity
@NoArgsConstructor
public class DayEntity extends DomainElement {
    private String date;
    private String dateId;
    private String dayName;
    private String dayNameEn;
    @ManyToOne
    @JoinColumn(name = "competition_id")
    private CompetitionEntity competition;
    @OneToMany(mappedBy = "day", cascade = CascadeType.ALL)
    private List<EventEntity> events;

    public DayEntity(String date, String dateId, String dayName, String dayNameEn) {
        this.date = date;
        this.dateId = dateId;
        this.dayName = dayName;
        this.dayNameEn = dayNameEn;
        events = new ArrayList<>();
    }

    public void addEvent(EventEntity event) {
        events.add(event);
    }

    public boolean isTheSame(DayEntity day) {
        return this.date.equals(day.getDate()) &&
                this.dateId.equals(day.getDateId()) &&
                this.dayName.equals(day.getDayName()) &&
                this.dayNameEn.equals(day.getDayNameEn());
    }

    @Override
    public String toString() {
        return "DayEntity{" +
                "date='" + date + '\'' +
                ", dateId='" + dateId + '\'' +
                ", dayName='" + dayName + '\'' +
                ", dayNameEn='" + dayNameEn + '\'' +
                '}';
    }

    public boolean isNeedToUpdate() {
        if (events.isEmpty()) {
            return true;
        }
        return events.stream().anyMatch(EventEntity::isNeedToUpdate);
    }
}

package com.dp_ua.iksparser.element;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@ToString
@Getter
@Setter
@Slf4j
@Entity
@NoArgsConstructor
@EqualsAndHashCode
public class DayEntity extends DomainElement {
    private String date;
    private String dateId;
    private String dayName;
    private String dayNameEn;
    @ManyToOne
    @JoinColumn(name = "competition_id")
    private CompetitionEntity competition;

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

    public List<Match> findMatchesBySurname(String surname) {
        List<Match> matches = new ArrayList<>();
//        String strToMatch = surname.toLowerCase();
//
//        for (EventEntity event : events) {
//            for (HeatEntity heat : event.getHeats()) {
//                for (ParticipantEntity participant : heat.getParticipants()) {
//                    if (participant.getSurname().toLowerCase().equals(strToMatch)) {
//                        // Найдено совпадение - сохраняем информацию о нем
//                        matches.add(new Match(this, event, heat, participant));
//                    }
//                }
//            }
//        }
        return matches;
    }
}

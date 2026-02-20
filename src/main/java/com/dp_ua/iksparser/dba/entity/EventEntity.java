package com.dp_ua.iksparser.dba.entity;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.dp_ua.iksparser.service.UrlUtils;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Entity
@Slf4j
@NoArgsConstructor
public class EventEntity extends DomainElement {

    private String time;
    private String eventName;
    private String category;
    private String round;
    private String startListUrl;
    private String resultUrl;
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<HeatEntity> heats;
    @ManyToOne
    @JoinColumn(name = "day_id")
    private DayEntity day;

    public boolean isTheSame(EventEntity event) {
        return this.eventName.equals(event.getEventName()) &&
                this.category.equals(event.getCategory()) &&
                this.round.equals(event.getRound());
    }

    public String getStartListUrl() {
        return UrlUtils.encodeUrl(startListUrl);
    }

    public String getResultUrl() {
        return UrlUtils.encodeUrl(resultUrl);
    }

    public void updateEventDetails(EventEntity event) {
        this.time = event.getTime();
        this.startListUrl = event.getStartListUrl();
        this.resultUrl = event.getResultUrl();
    }

    public boolean isNotFilled() {
        return startListUrl.isEmpty() && resultUrl.isEmpty();
    }

    @Override
    public String toString() {
        return "EventEntity{" +
                "time='" + time + '\'' +
                ", eventName='" + eventName + '\'' +
                ", category='" + category + '\'' +
                ", round='" + round + '\'' +
                ", startListUrl='" + startListUrl + '\'' +
                ", ResultUrl='" + resultUrl + '\'' +
                '}';
    }


    public EventEntity(String time, String eventName, String category, String round, String startListUrl, String resultUrl) {
        this.time = time;
        this.eventName = eventName;
        this.category = category;
        this.round = round;
        this.startListUrl = startListUrl;
        this.resultUrl = resultUrl;
        heats = new ArrayList<>();
    }

    public void addHeat(HeatEntity heat) {
        heats.add(heat);
    }

    public boolean isNeedToUpdate() {
        if (heats.isEmpty()) {
            return true;
        }
        return isNotFilled();
    }

    public boolean hasStartListUrl() {
        return StringUtils.isNotEmpty(startListUrl);
    }

    public boolean hasResultUrl() {
        return StringUtils.isNotEmpty(resultUrl);
    }

}

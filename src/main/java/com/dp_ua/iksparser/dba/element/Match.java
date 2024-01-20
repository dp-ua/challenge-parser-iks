package com.dp_ua.iksparser.dba.element;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter@Setter
@EqualsAndHashCode
public class Match {
    private DayEntity day;
    private EventEntity event;
    private HeatEntity heat;
    private ParticipantEntity participant;
    // I think this is not needed
    // todo remove when sure
}
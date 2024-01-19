package com.dp_ua.iksparser.element;

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
    private Heat heat;
    private Participant participant;
}